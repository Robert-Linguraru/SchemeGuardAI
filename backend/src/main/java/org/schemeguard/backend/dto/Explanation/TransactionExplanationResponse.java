package org.schemeguard.backend.dto.Explanation;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionExplanationResponse(
        UUID transactionId,
        String externalId,
        String qualificationStatus,
        String qualificationCategory,
        String originalExplanation,
        String llmExplanation,
        BigDecimal interchangeRate,
        BigDecimal interchangeFee,
        String currencyCode
) {
    public TransactionExplanationResponse(
            UUID transactionId,
            String externalId,
            String qualificationStatus,
            String originalExplanation,
            String llmExplanation,
            BigDecimal estimatedFee,
            String currencyCode
    ) {
        this(
                transactionId,
                externalId,
                qualificationStatus,
                null,
                originalExplanation,
                llmExplanation,
                null,
                estimatedFee,
                currencyCode
        );
    }
}