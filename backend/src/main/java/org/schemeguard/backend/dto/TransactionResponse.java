package org.schemeguard.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String externalId,
        String merchantName,
        String schemeCode,
        String schemeName,
        BigDecimal amount,
        String currency,
        String cardType,
        String cardCategory,
        String channel,
        String issuerCountry,
        String merchantCountry,
        String transactionStatus,
        String qualificationStatus,
        String qualificationCategory,
        String qualificationExplanation,
        Instant evaluatedAt,
        Instant createdAt
) {
}