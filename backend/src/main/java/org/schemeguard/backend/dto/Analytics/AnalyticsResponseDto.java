package org.schemeguard.backend.dto.Analytics;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsResponseDto(
        long totalTransactions,
        long processedTransactions,
        long qualifiedTransactions,
        long partiallyQualifiedTransactions,
        long notQualifiedTransactions,
        BigDecimal totalAmount,
        BigDecimal totalFees,
        BigDecimal averageTransactionAmount,
        BigDecimal qualificationRate,
        List<AnalyticsMetricDto> qualificationResults,
        List<AnalyticsMetricDto> feesByScheme,
        List<AnalyticsMetricDto> transactionsByCountry,
        List<AnalyticsMetricDto> topMerchantsByFees
) {
}
