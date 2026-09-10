package org.schemeguard.backend.service;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.entity.Rule;
import org.schemeguard.backend.repository.CardSchemeRepository;
import org.schemeguard.backend.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final CardSchemeRepository cardSchemeRepository;
    private final ObjectMapper objectMapper;

    public Rule createRule(RuleRequestDto dto) {
        CardScheme scheme = cardSchemeRepository
                .findById(dto.getScheme_id())
                .orElseThrow(() -> new RuntimeException("Card scheme not found")); //todo create exception

        Rule rule = new Rule();

        rule.setScheme(scheme);
        rule.setRuleCode(dto.getRuleCode());
        rule.setRuleName(dto.getRuleName());
        rule.setRegion(dto.getRegion());
        rule.setPriority(dto.getPriority());

        JsonNode conditionsJson = objectMapper.valueToTree(dto.getConditions());
        rule.setConditions(conditionsJson);

        rule.setQualificationCategory(
                dto.getResult().getQualificationCategory()
        );
        rule.setInterchangeRate(
                dto.getResult().getInterchangeRate()
        );
        rule.setEffectiveFrom(dto.getEffectiveFrom());
        rule.setEffectiveTo(dto.getEffectiveTo());
        rule.setVersion(dto.getVersion());
        rule.setActive(dto.isActive());

        return ruleRepository.save(rule);
    }
}
