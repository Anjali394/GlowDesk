package com.glowdesk.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

public record AvailableSlotsResponse(
        @Schema(example = "45")
        int totalDuration,

        @Schema(type = "string", example = "09:00:00")
        LocalTime openingTime,

        @Schema(type = "string", example = "21:00:00")
        LocalTime closingTime,

        @Schema(type = "array", example = "[\"09:00:00\", \"09:30:00\", \"10:00:00\"]")
        List<LocalTime> availableSlots
) {}
