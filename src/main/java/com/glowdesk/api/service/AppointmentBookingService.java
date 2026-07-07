package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.BookAppointmentRequest;
import com.glowdesk.api.dto.response.AppointmentResponse;
import com.glowdesk.api.dto.response.AppointmentServiceResponse;
import com.glowdesk.api.dto.response.AvailableSlotsResponse;
import com.glowdesk.api.entity.*;
import com.glowdesk.api.enums.AppointmentStatus;
import com.glowdesk.api.enums.DiscountType;
import com.glowdesk.api.exception.BadRequestException;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        // Generate slots every 30 minutes within operating hours
        // For today: start from next 30-min boundary after current time
        // For future dates: start from branch opening time
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
            LocalTime slotEnd = cursor.plusMinutes(totalDuration);
            List<Stylist> available = stylistRepository.findAvailableStylists(
                    branchId, date, cursor, slotEnd);
            if (!available.isEmpty()) {
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
        Stylist stylist = available.get(0); // highest rated — query orders by rating DESC

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

    // --- Helpers ---

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
