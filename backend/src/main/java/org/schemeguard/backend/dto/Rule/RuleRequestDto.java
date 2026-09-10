package org.schemeguard.backend.dto.Rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RuleRequestDto {
    @JsonProperty("scheme_id")
    private UUID schemeId;

    @JsonProperty("rule_code")
    private String ruleCode;

    @JsonProperty("rule_name")
    private String ruleName;
    private String region;
    private int priority;
    private Map<String, Object> conditions;
    private RuleResultDto result;

    @JsonProperty("effective_from")
    private LocalDate effectiveFrom;

    @JsonProperty("effective_to")
    private LocalDate effectiveTo;
    private int version;
    private boolean active;
}
