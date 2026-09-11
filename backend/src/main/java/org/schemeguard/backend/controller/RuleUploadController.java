package org.schemeguard.backend.controller;

import jakarta.validation.Valid;
import org.schemeguard.backend.dto.Rule.RuleDetailsResponseDto;
import org.schemeguard.backend.dto.Rule.RuleListResponseDto;
import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.schemeguard.backend.entity.Rule;
import org.schemeguard.backend.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/rule")
public class RuleUploadController {

    private final Logger logger = Logger.getLogger(RuleUploadController.class.getName());
    private final RuleService ruleService;

    @Autowired
    public RuleUploadController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    private RuleListResponseDto toRuleListResponseDto(Rule rule) {
        return new RuleListResponseDto(
                rule.getId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getScheme().getCode(),
                rule.getScheme().getName(),
                rule.getRegion(),
                rule.getQualificationCategory(),
                rule.getInterchangeRate(),
                rule.getPriority(),
                rule.getVersion(),
                rule.getActive(),
                rule.getEffectiveFrom(),
                rule.getEffectiveTo()
        );
    }

    private RuleDetailsResponseDto toRuleDetailsResponseDto(Rule rule) {
        return new RuleDetailsResponseDto(
                rule.getId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getScheme().getCode(),
                rule.getScheme().getName(),
                rule.getRegion(),
                rule.getPriority(),
                rule.getConditions(),
                rule.getQualificationCategory(),
                rule.getInterchangeRate(),
                rule.getEffectiveFrom(),
                rule.getEffectiveTo(),
                rule.getVersion(),
                rule.getActive(),
                rule.getCreatedAt()
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<RuleListResponseDto> uploadRule(
            @Valid @RequestBody RuleRequestDto ruleRequestDto
    ) {
        Rule response = ruleService.createRule(ruleRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toRuleListResponseDto(response));
    }

    @GetMapping("/getRule")
    public ResponseEntity<RuleDetailsResponseDto> getRule(
            @RequestParam UUID id
    ) {
        Rule response = ruleService.getRuleById(id);

        return ResponseEntity.ok(toRuleDetailsResponseDto(response));
    }

    @GetMapping("/getRules")
    public ResponseEntity<List<RuleListResponseDto>> getRules() {
        var rules = ruleService.getAllRules();

        List<RuleListResponseDto> response = StreamSupport
                .stream(rules.spliterator(), false)
                .map(this::toRuleListResponseDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteRule(
            @RequestParam UUID id
    ) {
        ruleService.deleteRule(id);

        return ResponseEntity.noContent().build();
    }
}
