package com.glowdesk.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateServiceRequest(
        String name,
        String description,

        @Min(1)
        Integer duration,

        @DecimalMin("0.01")
        BigDecimal price
) {}
