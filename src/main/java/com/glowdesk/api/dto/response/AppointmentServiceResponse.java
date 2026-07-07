package com.glowdesk.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentServiceResponse(
        @Schema(example = "471bcaba-c67a-45e9-a737-b5d6c69d909e")
        UUID serviceId,

        @Schema(example = "Body Wax")
        String name,

        @Schema(example = "45")
        int duration,

        @Schema(example = "1000.00")
        BigDecimal priceAtBooking
) {}
