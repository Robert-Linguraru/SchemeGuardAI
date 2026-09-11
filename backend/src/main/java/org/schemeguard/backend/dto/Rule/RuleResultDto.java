package org.schemeguard.backend.dto.Rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RuleResultDto {
    @JsonProperty("qualification_category")
    private String qualificationCategory;

    @JsonProperty("interchange_rate")
    private BigDecimal interchangeRate;

    @JsonProperty("fee_type")
    private String feeType;
}
