package com.glowdesk.api.dto.request;

import com.glowdesk.api.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateComboRequest(
        @NotNull
        @Schema(example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
        UUID branchId,

        @NotBlank
        @Schema(example = "Bridal Package")
        String name,

        @Schema(example = "Hair Cut + Face Cleanup combo")
        String description,

        @NotNull
        @Schema(example = "PERCENTAGE")
        DiscountType discountType,

        @NotNull @DecimalMin("0.01")
        @Schema(example = "10.00")
        BigDecimal discountValue,

        @NotEmpty
        @Schema(example = "[\"471bcaba-c67a-45e9-a737-b5d6c69d909e\"]")
        Set<UUID> serviceIds
) {}
