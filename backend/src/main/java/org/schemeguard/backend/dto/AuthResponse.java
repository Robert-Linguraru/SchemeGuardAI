package org.schemeguard.backend.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        UUID id,
        String email,
        String fullName,
        String status
) {
}