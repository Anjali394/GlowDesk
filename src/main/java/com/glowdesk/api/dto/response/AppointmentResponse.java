package com.glowdesk.api.dto.response;

import com.glowdesk.api.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AppointmentResponse(
        @Schema(example = "bb86a881-1a55-41c2-bbcb-4c236c200844")
        UUID id,

        @Schema(example = "09bb54f3-c81f-4a05-bfaf-f2c571b984f7")
        UUID customerId,

        @Schema(example = "1ea667e1-6243-4cd3-ab4d-994fa83ede3f")
        UUID stylistId,

        @Schema(example = "Priya Singh")
        String stylistName,

        @Schema(example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
        UUID branchId,

        @Schema(example = "257bcd90-f758-40fe-9a72-e01f18a1c7d8")
        UUID comboId,

        @Schema(example = "PENDING")
        AppointmentStatus status,

        @Schema(type = "string", example = "2026-07-08")
        LocalDate scheduledDate,

        @Schema(type = "string", example = "10:00:00")
        LocalTime startTime,

        @Schema(type = "string", example = "11:30:00")
        LocalTime endTime,

        @Schema(example = "90")
        int totalDuration,

        @Schema(example = "3500.00")
        BigDecimal totalPrice,

        @Schema(example = "2026-07-07T13:14:50.731327Z")
        OffsetDateTime expiresAt,

        List<AppointmentServiceResponse> services
) {}
