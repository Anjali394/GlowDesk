package com.glowdesk.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleAppointmentRequest(
        @NotNull @Future LocalDate scheduledDate,
        @NotNull LocalTime startTime
) {}
