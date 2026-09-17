package org.schemeguard.backend.dto.CardSchemes.Responses;

import java.util.List;

public record GetAllCardSchemesResponse(
        List<CardSchemeResponse> cardSchemes,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String search,
        String sort,
        boolean includeInactive
) {}
