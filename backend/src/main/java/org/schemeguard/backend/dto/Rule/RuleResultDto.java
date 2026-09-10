package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RuleResultDto {
    private String qualificationCategory;
    private BigDecimal interchangeRate;
    private String feeType;
}
