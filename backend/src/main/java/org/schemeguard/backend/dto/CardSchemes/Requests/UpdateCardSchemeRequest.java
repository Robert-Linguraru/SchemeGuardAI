package org.schemeguard.backend.dto.CardSchemes.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCardSchemeRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 100) String name
) {}
