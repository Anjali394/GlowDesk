package com.glowdesk.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateServiceRequest(
        @NotNull
        @Schema(example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
        UUID branchId,

        @NotBlank
        @Schema(example = "Hair Cut")
        String name,

        @Schema(example = "Classic hair cut for all hair types")
        String description,

        @Min(1)
        @Schema(example = "30", description = "Duration in minutes")
        int duration,

        @NotNull @DecimalMin("0.01")
        @Schema(example = "300.00")
        BigDecimal price
) {}
