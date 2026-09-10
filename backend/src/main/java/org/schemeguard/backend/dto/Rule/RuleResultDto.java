package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RuleResultDto {
    private String qualification_category;
    private BigDecimal interchange_rate;
    private String fee_type;
}
