package org.schemeguard.backend.dto.CardSchemes.Responses;

import java.util.List;

public record GetAllCardSchemesResponse(List<CardSchemeResponse> cardSchemes) {}
