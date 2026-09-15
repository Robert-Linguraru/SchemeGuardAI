package org.schemeguard.backend.dto.CardSchemes.Responses;

import java.util.UUID;

public record CardSchemeResponse(UUID id, String code, String name, Boolean active) {}
