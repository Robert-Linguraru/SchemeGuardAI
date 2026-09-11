package org.schemeguard.backend.dto.Rule;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RuleListResponseDto(
        UUID id,
        String ruleCode,
        String ruleName,
        String schemeCode,
        String schemeName,
        String region,
        String qualificationCategory,
        BigDecimal interchangeRate,
        Integer priority,
        Integer version,
        Boolean active,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}