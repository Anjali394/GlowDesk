package com.glowdesk.api.controller;

import com.glowdesk.api.dto.request.BookAppointmentRequest;
import com.glowdesk.api.dto.request.RejectAppointmentRequest;
import com.glowdesk.api.dto.response.AppointmentResponse;
import com.glowdesk.api.dto.response.AvailableSlotsResponse;
import com.glowdesk.api.service.AppointmentBookingService;
import com.glowdesk.api.service.ReceptionistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name = "Appointment Module")
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentBookingService appointmentBookingService;
    private final ReceptionistService receptionistService;

    // --- Public ---

    @Operation(summary = "Get available time slots for a date and services [Public]")
    @GetMapping("/slots")
    public AvailableSlotsResponse getAvailableSlots(
            @Parameter(example = "57929888-e65f-4dce-a0b1-bebd04e3594e") @RequestParam UUID branchId,
            @Parameter(example = "2026-07-08") @RequestParam LocalDate date,
            @Parameter(example = "257bcd90-f758-40fe-9a72-e01f18a1c7d8") @RequestParam(required = false) UUID comboId,
            @RequestParam(required = false) Set<UUID> serviceIds) {
        return appointmentBookingService.getAvailableSlots(branchId, date, serviceIds, comboId);
    }

    // --- Customer ---

    @Operation(summary = "Book an appointment [Customer]")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public AppointmentResponse book(@Valid @RequestBody BookAppointmentRequest request) {
        return appointmentBookingService.book(request);
    }

    @Operation(summary = "View my appointment history [Customer]")
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<AppointmentResponse> getMyAppointments() {
        return appointmentBookingService.getMyAppointments();
    }

    // --- Receptionist ---

    @Operation(summary = "View all pending appointments for a branch [Receptionist]")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public List<AppointmentResponse> getPending(
            @Parameter(example = "57929888-e65f-4dce-a0b1-bebd04e3594e") @RequestParam UUID branchId) {
        return receptionistService.getPendingAppointments(branchId);
    }

    @Operation(summary = "Confirm a pending appointment [Receptionist]")
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public AppointmentResponse confirm(@PathVariable UUID id) {
        return receptionistService.confirm(id);
    }

    @Operation(summary = "Reject a pending appointment with optional reason [Receptionist]")
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public AppointmentResponse reject(@PathVariable UUID id,
                                      @RequestBody RejectAppointmentRequest request) {
        return receptionistService.reject(id, request);
    }

    @Operation(summary = "Mark a confirmed appointment as completed [Receptionist]")
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public AppointmentResponse complete(@PathVariable UUID id) {
        return receptionistService.complete(id);
    }
}
