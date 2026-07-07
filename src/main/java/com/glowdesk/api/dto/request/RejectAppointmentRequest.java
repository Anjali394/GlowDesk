package com.glowdesk.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RejectAppointmentRequest(
        @Schema(example = "No stylist available for this time slot")
        String reason
) {}
