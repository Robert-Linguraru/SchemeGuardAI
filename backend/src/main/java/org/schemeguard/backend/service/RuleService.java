package org.schemeguard.backend.service;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.entity.Rule;
import org.schemeguard.backend.repository.CardSchemeRepository;
import org.schemeguard.backend.repository.RuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final CardSchemeRepository cardSchemeRepository;

    public Rule createRule(RuleRequestDto dto) {
        CardScheme scheme = cardSchemeRepository
                .findById(dto.getSchemeId())
                .orElseThrow(() -> new RuntimeException("Card scheme not found")); //todo create exception

        Rule rule = new Rule();

        rule.setScheme(scheme);
        rule.setRuleCode(dto.getRuleCode());
        rule.setRuleName(dto.getRuleName());
        rule.setRegion(dto.getRegion());
        rule.setPriority(dto.getPriority());

        rule.setConditions(dto.getConditions());

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

    public Rule getRuleById(UUID id) {
        return ruleRepository.findRuleById(id);
    }

    public Iterable<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    @Transactional
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Rule not found"
            );
        }

        ruleRepository.deleteById(id);
    }
}
