package org.schemeguard.backend.dto.Analytics;

import java.math.BigDecimal;

public record AnalyticsMetricDto(String name, long transactions, BigDecimal fees) {
}
