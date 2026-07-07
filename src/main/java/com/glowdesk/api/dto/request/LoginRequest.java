package com.glowdesk.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email
        @Schema(example = "anjalirajput394@gmail.com")
        String email,

        @NotBlank
        @Schema(example = "xxxxxxxx")
        String password
) {}
