package com.glowdesk.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record StylistResponse(
        UUID id,
        String firstName,
        String lastName,
        int experience,
        BigDecimal rating,
        boolean isActive
) {}
