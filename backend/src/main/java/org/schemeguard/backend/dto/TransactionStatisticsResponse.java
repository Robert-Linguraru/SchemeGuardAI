package org.schemeguard.backend.dto;

import java.math.BigDecimal;

public record TransactionStatisticsResponse(
        long totalTransactionCount,
        long qualifiedCount,
        long notQualifiedCount,
        long partiallyQualifiedCount,
        long withoutQualificationResultCount,
        BigDecimal totalTransactionAmount
) {
}