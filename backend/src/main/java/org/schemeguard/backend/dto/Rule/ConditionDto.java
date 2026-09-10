package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
public class ConditionDto {
    private String field;
    private String operator;
    private JsonNode value;
}