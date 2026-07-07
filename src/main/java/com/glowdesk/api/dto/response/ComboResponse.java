package com.glowdesk.api.dto.response;

import com.glowdesk.api.enums.DiscountType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ComboResponse(
        UUID id,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        Set<ServiceResponse> services,
        boolean isActive
) {}
