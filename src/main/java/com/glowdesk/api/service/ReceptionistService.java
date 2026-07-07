package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.RejectAppointmentRequest;
import com.glowdesk.api.dto.response.AppointmentResponse;
import com.glowdesk.api.dto.response.AppointmentServiceResponse;
import com.glowdesk.api.entity.Appointment;
import com.glowdesk.api.entity.AppointmentService;
import com.glowdesk.api.entity.Customer;
import com.glowdesk.api.enums.AppointmentStatus;
import com.glowdesk.api.exception.BadRequestException;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;

    public List<AppointmentResponse> getPendingAppointments(UUID branchId) {
        return appointmentRepository.findByBranchIdAndStatus(branchId, AppointmentStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse confirm(UUID id) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING appointments can be confirmed. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        Customer customer = appointment.getCustomer();

        // Send confirmation email immediately
        emailService.sendConfirmationEmail(customer, appointment);

        // Calculate reminder time = appointment start - 30 minutes
        LocalDateTime appointmentStart = appointment.getScheduledDate()
                .atTime(appointment.getStartTime());
        LocalDateTime reminderTime = appointmentStart.minusMinutes(30);
        Instant now = Instant.now();
        Instant reminderInstant = reminderTime.toInstant(ZoneOffset.UTC);

        if (!reminderInstant.isAfter(now)) {
            // Appointment is within 30 minutes — send reminder immediately
            emailService.sendReminderEmail(customer, appointment);
        } else {
            // Schedule reminder for exactly 30 minutes before start
            taskScheduler.schedule(
                    () -> emailService.sendReminderEmail(customer, appointment),
                    reminderInstant);
        }

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse reject(UUID id, RejectAppointmentRequest request) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING appointments can be rejected. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.setRejectionReason(request.reason());
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse complete(UUID id) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Only CONFIRMED appointments can be completed. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(appointmentRepository.save(appointment));
    }

    // --- Helpers ---

    private Appointment findAppointment(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private AppointmentResponse toResponse(Appointment a) {
        int totalDuration = a.getAppointmentServices().stream()
                .mapToInt(AppointmentService::getDuration)
                .sum();

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
