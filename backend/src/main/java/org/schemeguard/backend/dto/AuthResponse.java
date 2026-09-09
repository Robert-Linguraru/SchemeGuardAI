package org.schemeguard.backend.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String email,
        String fullName,
        String status
) {
}