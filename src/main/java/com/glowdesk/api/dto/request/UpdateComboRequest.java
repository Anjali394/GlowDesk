package com.glowdesk.api.dto.request;

import com.glowdesk.api.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record UpdateComboRequest(
        String name,
        String description,
        DiscountType discountType,

        @DecimalMin("0.01")
        BigDecimal discountValue,

        Set<UUID> serviceIds
) {}
