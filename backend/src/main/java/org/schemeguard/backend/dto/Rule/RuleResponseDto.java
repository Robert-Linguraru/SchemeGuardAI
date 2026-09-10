package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class RuleResponseDto {
    private UUID id;
    private String ruleCode;
    private String ruleName;
    private String qualificationCategory;
    private BigDecimal interchangeRate;

    public RuleResponseDto(UUID id, String ruleCode, String ruleName, String qualificationCategory, BigDecimal interchangeRate) {
        this.id = id;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.qualificationCategory = qualificationCategory;
        this.interchangeRate = interchangeRate;
    }
}
