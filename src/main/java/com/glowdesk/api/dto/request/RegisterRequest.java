package com.glowdesk.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email
        @Schema(example = "anjalirajput394@gmail.com")
        String email,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        @Schema(example = "xxxxxxxx")
        String password,

        @NotBlank
        @Schema(example = "Anjali")
        String firstName,

        @Schema(example = "Rajput")
        String lastName,

        @Schema(example = "+xx xxxxxxxxxx")
        String phone
) {}
