package org.schemeguard.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionMockResponse(
        UUID id,
        String externalId,
        String merchantName,
        String scheme,
        BigDecimal amount,
        String currencyCode,
        String cardType,
        String cardCategory,
        String channel,
        String issuerCountry,
        String merchantCountry,
        String status,
        String qualificationStatus,
        BigDecimal estimatedFee,
        String explanation,
        OffsetDateTime authorizedAt
) {
}