package com.glowdesk.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

public record BranchResponse(
        @Schema(example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
        UUID id,

        @Schema(example = "GlowDesk Koramangala")
        String name,

        @Schema(example = "27, 80 Feet Rd, Koramangala 4th Block")
        String address,

        @Schema(example = "Bengaluru")
        String city,

        @Schema(example = "Karnataka")
        String state,

        @Schema(example = "+91 98765 43210")
        String phone,

        @Schema(type = "string", example = "09:00:00")
        LocalTime openingTime,

        @Schema(type = "string", example = "21:00:00")
        LocalTime closingTime
) {}
