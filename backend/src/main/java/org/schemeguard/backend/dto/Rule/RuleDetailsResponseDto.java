package org.schemeguard.backend.dto.Rule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record RuleDetailsResponseDto(
        UUID id,
        String ruleCode,
        String ruleName,
        String schemeCode,
        String schemeName,
        String region,
        Integer priority,
        Map<String, Object> conditions,
        String qualificationCategory,
        BigDecimal interchangeRate,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Integer version,
        Boolean active,
        Instant createdAt
) {}