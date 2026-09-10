package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class RuleRequestDto {
    private UUID scheme_id;
    private String ruleCode;
    private String ruleName;
    private String region;
    private int priority;
    private ConditionsDto conditions;
    private RuleResultDto result;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private int version;
    private boolean active;
}
