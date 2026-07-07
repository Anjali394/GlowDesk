package com.glowdesk.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record BookAppointmentRequest(
        @NotNull
        @Schema(example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
        UUID branchId,

        @Schema(example = "[\"471bcaba-c67a-45e9-a737-b5d6c69d909e\"]")
        Set<UUID> serviceIds,

        @Schema(example = "257bcd90-f758-40fe-9a72-e01f18a1c7d8")
        UUID comboId,

        @NotNull @FutureOrPresent
        @Schema(example = "2026-07-08")
        LocalDate scheduledDate,

        @NotNull
        @Schema(example = "10:00:00")
        LocalTime startTime
) {}
