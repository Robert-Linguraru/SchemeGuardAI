package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class RuleRequestDto {
    private UUID scheme_id;
    private String rule_code;
    private String rule_name;
    private String region;
    private int priority;
    private ConditionsDto conditions;
    private RuleResultDto result;
    private Date effective_from;
    private Date effective_to;
    private int version;
    private boolean active;
}
