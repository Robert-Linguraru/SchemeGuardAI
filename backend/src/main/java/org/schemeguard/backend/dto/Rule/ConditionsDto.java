package org.schemeguard.backend.dto.Rule;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConditionsDto {
    private List<ConditionDto> all;
}
