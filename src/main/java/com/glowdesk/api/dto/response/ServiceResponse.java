package com.glowdesk.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(
        @Schema(example = "471bcaba-c67a-45e9-a737-b5d6c69d909e")
        UUID id,

        @Schema(example = "Hair Cut")
        String name,

        @Schema(example = "Step cut for all hair types")
        String description,

        @Schema(example = "30", description = "Duration in minutes")
        int duration,

        @Schema(example = "800.00")
        BigDecimal price,

        @Schema(example = "true")
        boolean isActive
) {}
