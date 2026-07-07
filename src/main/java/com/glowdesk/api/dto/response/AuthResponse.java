package com.glowdesk.api.dto.response;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        String message,
        String token,
        UUID userId,
        String email,
        Set<String> roles
) {}