package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.BookAppointmentRequest;
import com.glowdesk.api.dto.request.RescheduleAppointmentRequest;
import com.glowdesk.api.dto.response.AppointmentResponse;
import com.glowdesk.api.dto.response.AppointmentServiceResponse;
import com.glowdesk.api.dto.response.AvailableSlotsResponse;
import com.glowdesk.api.entity.*;
import com.glowdesk.api.enums.AppointmentStatus;
import com.glowdesk.api.enums.DiscountType;
import com.glowdesk.api.enums.Gender;
import com.glowdesk.api.exception.BadRequestException;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final ComboRepository comboRepository;
    private final StylistRepository stylistRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final TaskScheduler taskScheduler;

    public AvailableSlotsResponse getAvailableSlots(UUID branchId, LocalDate date,
                                                     Set<UUID> serviceIds, UUID comboId) {
        boolean hasServices = serviceIds != null && !serviceIds.isEmpty();
        boolean hasCombo = comboId != null;

        if (!hasServices && !hasCombo) {
            throw new BadRequestException("Provide either serviceIds or a comboId");
        }
        if (hasServices && hasCombo) {
            throw new BadRequestException("Provide either serviceIds or a comboId, not both");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));

        List<com.glowdesk.api.entity.Service> services;
        if (hasCombo) {
            Combo combo = comboRepository.findById(comboId)
                    .orElseThrow(() -> new ResourceNotFoundException("Combo not found: " + comboId));
            services = List.copyOf(combo.getServices());
        } else {
            services = serviceRepository.findAllById(serviceIds);
        }

        int totalDuration = services.stream()
                .mapToInt(com.glowdesk.api.entity.Service::getDuration)
                .sum();

        // 2 queries instead of one per slot
        List<Stylist> allStylists = stylistRepository.findByBranchIdAndIsActiveTrueOrderByRatingDesc(branchId);
        List<Appointment> booked = appointmentRepository.findBookedAppointmentsWithStylist(branchId, date);

        List<LocalTime> slots = new ArrayList<>();
        LocalTime cursor;
        if (date.isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            int minutesToNextSlot = 30 - (now.getMinute() % 30);
            LocalTime nextSlot = now.plusMinutes(minutesToNextSlot).withSecond(0).withNano(0);
            cursor = nextSlot.isBefore(branch.getOpeningTime()) ? branch.getOpeningTime() : nextSlot;
        } else {
            cursor = branch.getOpeningTime();
        }
        LocalTime latestStart = branch.getClosingTime().minusMinutes(totalDuration);

        while (!cursor.isAfter(latestStart)) {
            final LocalTime slotStart = cursor;
            final LocalTime slotEnd = cursor.plusMinutes(totalDuration);

            Set<UUID> busyIds = booked.stream()
                    .filter(a -> a.getStartTime().isBefore(slotEnd) && a.getEndTime().isAfter(slotStart))
                    .map(a -> a.getStylist().getId())
                    .collect(Collectors.toSet());

            if (allStylists.stream().anyMatch(s -> !busyIds.contains(s.getId()))) {
                slots.add(cursor);
            }
            cursor = cursor.plusMinutes(30);
        }

        return new AvailableSlotsResponse(totalDuration, branch.getOpeningTime(),
                branch.getClosingTime(), slots);
    }

    @Transactional
    public AppointmentResponse book(BookAppointmentRequest request) {

        // Phase 1: Validate — serviceIds OR comboId, not both, not neither
        boolean hasServices = request.serviceIds() != null && !request.serviceIds().isEmpty();
        boolean hasCombo = request.comboId() != null;

        if (!hasServices && !hasCombo) {
            throw new BadRequestException("Provide either serviceIds or a comboId");
        }
        if (hasServices && hasCombo) {
            throw new BadRequestException("Provide either serviceIds or a comboId, not both");
        }

        // Phase 2: Resolve branch and customer
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + request.branchId()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = resolveCustomer(email);

        // Phase 3: Resolve services list
        List<com.glowdesk.api.entity.Service> services;
        Combo combo = null;

        if (hasCombo) {
            combo = comboRepository.findById(request.comboId())
                    .orElseThrow(() -> new ResourceNotFoundException("Combo not found: " + request.comboId()));
            services = List.copyOf(combo.getServices());
        } else {
            services = serviceRepository.findAllById(request.serviceIds());
            if (services.size() != request.serviceIds().size()) {
                throw new ResourceNotFoundException("One or more service IDs not found");
            }
        }

        // Phase 4: Calculate total duration and endTime
        int totalDuration = services.stream()
                .mapToInt(com.glowdesk.api.entity.Service::getDuration)
                .sum();
        LocalTime endTime = request.startTime().plusMinutes(totalDuration);

        // Phase 5: Auto-assign stylist
        List<Stylist> available = stylistRepository.findAvailableStylists(
                branch.getId(), request.scheduledDate(), request.startTime(), endTime);

        if (available.isEmpty()) {
            throw new BadRequestException(
                    "No available stylist for " + request.scheduledDate() +
                    " from " + request.startTime() + " to " + endTime);
        }
        Stylist stylist = selectStylist(available, customer.getGender());

        // Phase 6: Snapshot prices and calculate total
        BigDecimal totalPrice = services.stream()
                .map(com.glowdesk.api.entity.Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (combo != null) {
            if (combo.getDiscountType() == DiscountType.PERCENTAGE) {
                BigDecimal discount = totalPrice.multiply(combo.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalPrice = totalPrice.subtract(discount);
            } else {
                totalPrice = totalPrice.subtract(combo.getDiscountValue()).max(BigDecimal.ZERO);
            }
        }

        // Phase 7: Build and save the appointment
        Appointment appointment = Appointment.builder()
                .branch(branch)
                .customer(customer)
                .stylist(stylist)
                .combo(combo)
                .status(AppointmentStatus.PENDING)
                .scheduledDate(request.scheduledDate())
                .startTime(request.startTime())
                .endTime(endTime)
                .totalPrice(totalPrice)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .build();

        List<AppointmentService> appointmentServices = services.stream()
                .map(s -> AppointmentService.builder()
                        .appointment(appointment)
                        .service(s)
                        .priceAtBooking(s.getPrice())
                        .duration(s.getDuration())
                        .build())
                .toList();

        appointment.getAppointmentServices().addAll(appointmentServices);
        appointmentRepository.save(appointment);
        scheduleReceptionistNotification(appointment, customer);

        return toResponse(appointment, totalDuration);
    }

    public List<AppointmentResponse> getMyAppointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = resolveCustomer(email);

        return appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(a -> toResponse(a, totalDurationOf(a)))
                .toList();
    }

    @Transactional
    public AppointmentResponse cancel(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = resolveCustomer(email);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        if (!appointment.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Appointment does not belong to the current user");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Only PENDING or CONFIRMED appointments can be cancelled. Current status: "
                    + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment), totalDurationOf(appointment));
    }

    @Transactional
    public AppointmentResponse reschedule(UUID id, RescheduleAppointmentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = resolveCustomer(email);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        if (!appointment.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Appointment does not belong to the current user");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Only PENDING or CONFIRMED appointments can be rescheduled. Current status: "
                    + appointment.getStatus());
        }

        int totalDuration = totalDurationOf(appointment);
        LocalTime newEndTime = request.startTime().plusMinutes(totalDuration);

        List<Stylist> available = stylistRepository.findAvailableStylists(
                appointment.getBranch().getId(), request.scheduledDate(),
                request.startTime(), newEndTime);

        if (available.isEmpty()) {
            throw new BadRequestException(
                    "No available stylist for " + request.scheduledDate() +
                    " from " + request.startTime() + " to " + newEndTime);
        }

        appointment.setScheduledDate(request.scheduledDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(newEndTime);
        appointment.setStylist(selectStylist(available, customer.getGender()));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
        appointmentRepository.save(appointment);
        scheduleReceptionistNotification(appointment, customer);

        return toResponse(appointment, totalDuration);
    }

    // --- Helpers ---

    private Stylist selectStylist(List<Stylist> available, Gender customerGender) {
        if (customerGender == Gender.FEMALE) {
            return available.stream()
                    .filter(s -> s.getGender() == Gender.FEMALE)
                    .findFirst()
                    .orElse(available.get(0));
        }
        return available.get(0);
    }

    private void scheduleReceptionistNotification(Appointment appointment, Customer customer) {
        Instant notifyAt = appointment.getExpiresAt().minusMinutes(15).toInstant();
        String title = "Appointment requires action";
        String message = String.format(
                "Appointment for %s %s at %s on %s from %s expires in 15 minutes. Please confirm or reject.",
                customer.getFirstName(), customer.getLastName(),
                appointment.getBranch().getName(),
                appointment.getScheduledDate(),
                appointment.getStartTime());

        taskScheduler.schedule(() -> {
            List<User> receptionists = userRepository.findByRoles_Name("RECEPTIONIST");
            List<Notification> notifications = receptionists.stream()
                    .map(r -> Notification.builder()
                            .user(r)
                            .type("PENDING_ACTION")
                            .title(title)
                            .message(message)
                            .build())
                    .toList();
            notificationRepository.saveAll(notifications);
        }, notifyAt);
    }

    private Customer resolveCustomer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for: " + email));
    }

    private int totalDurationOf(Appointment a) {
        return a.getAppointmentServices().stream()
                .mapToInt(AppointmentService::getDuration)
                .sum();
    }

    private AppointmentResponse toResponse(Appointment a, int totalDuration) {
        List<AppointmentServiceResponse> services = a.getAppointmentServices().stream()
                .map(as -> new AppointmentServiceResponse(
                        as.getService().getId(),
                        as.getService().getName(),
                        as.getDuration(),
                        as.getPriceAtBooking()))
                .toList();

        return new AppointmentResponse(
                a.getId(),
                a.getCustomer().getId(),
                a.getStylist().getId(),
                a.getStylist().getFirstName() + " " + a.getStylist().getLastName(),
                a.getBranch().getId(),
                a.getBranch().getName(),
                a.getCombo() != null ? a.getCombo().getId() : null,
                a.getStatus(),
                a.getScheduledDate(),
                a.getStartTime(),
                a.getEndTime(),
                totalDuration,
                a.getTotalPrice(),
                a.getExpiresAt(),
                services);
    }
}
