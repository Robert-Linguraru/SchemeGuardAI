package org.schemeguard.backend.controller;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.schemeguard.backend.dto.Rule.RuleResponseDto;
import org.schemeguard.backend.entity.Rule;
import org.schemeguard.backend.repository.RuleRepository;
import org.schemeguard.backend.service.RuleService;
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

    public RuleUploadController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    private RuleResponseDto toResponseDto(Rule rule) {
        return new RuleResponseDto(
                rule.getId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getQualificationCategory(),
                rule.getInterchangeRate()
        );
    }

    @PostMapping("/upload")
    private ResponseEntity<RuleResponseDto> uploadRule(
            @Valid @RequestBody RuleRequestDto ruleRequestDto
    ) {
        Rule response = ruleService.createRule(ruleRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponseDto(response));
    }

    @GetMapping("/getRule")
    private ResponseEntity<RuleResponseDto> getRule(
            @RequestParam UUID id
    ) {
        Rule response = ruleService.getRuleById(id);

        return ResponseEntity.ok(toResponseDto(response));
    }

    @GetMapping("/getRules")
    private ResponseEntity<List<RuleResponseDto>> getRules() {
        var rules = ruleService.getAllRules();

        List<RuleResponseDto> response = StreamSupport
                .stream(rules.spliterator(), false)
                .map(this::toResponseDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    private ResponseEntity<Void> deleteRule(
            @RequestParam UUID id
    ) {
        ruleService.deleteRule(id);

        return ResponseEntity.noContent().build();
    }
}
