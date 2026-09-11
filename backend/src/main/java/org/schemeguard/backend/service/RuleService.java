package org.schemeguard.backend.service;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.entity.Rule;
import org.schemeguard.backend.exception.ConflictException;
import org.schemeguard.backend.exception.ResourceNotFoundException;
import org.schemeguard.backend.repository.CardSchemeRepository;
import org.schemeguard.backend.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final CardSchemeRepository cardSchemeRepository;

    public Rule createRule(RuleRequestDto dto) {
        CardScheme scheme = cardSchemeRepository
                .findById(dto.getSchemeId())
            .orElseThrow(() -> new ResourceNotFoundException("Card scheme not found"));

        if (ruleRepository.existsBySchemeIdAndRuleCodeAndVersion(
            dto.getSchemeId(), dto.getRuleCode(), dto.getVersion())) {
            throw new ConflictException("A rule with this code already exists.");
        }

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

    @Transactional(readOnly = true)
    public Rule getRuleById(UUID id) {
        return ruleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rule not found"));
    }

    @Transactional(readOnly = true)
    public Iterable<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    @Transactional
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rule not found");
        }

        ruleRepository.deleteById(id);
    }
}
