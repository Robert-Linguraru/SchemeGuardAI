package org.schemeguard.backend.dto.AccountManagement;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @Size(min = 8, max = 100)
        String password
) {
}
