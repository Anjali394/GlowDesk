package com.glowdesk.api.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateStylistRequest(
        String firstName,
        String lastName,

        @Min(0)
        Integer experience
) {}
