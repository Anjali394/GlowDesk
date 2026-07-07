package com.glowdesk.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateStylistRequest(
        @NotNull
        @Schema(example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
        UUID branchId,

        @NotBlank
        @Schema(example = "Priya")
        String firstName,

        @Schema(example = "Sharma")
        String lastName,

        @Min(0)
        @Schema(example = "3")
        int experience
) {}
