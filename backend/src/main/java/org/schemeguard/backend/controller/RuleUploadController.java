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

@RestController
@RequestMapping("/api/rule")
public class RuleUploadController {

    private final Logger logger = Logger.getLogger(RuleUploadController.class.getName());
    private final RuleService ruleService;

    public RuleUploadController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping("/upload")
    private ResponseEntity<RuleResponseDto> uploadRule(
            @Valid @RequestBody RuleRequestDto ruleRequestDto
    ) {
        Rule savedRule = ruleService.createRule(ruleRequestDto);

        RuleResponseDto response = new RuleResponseDto(
                savedRule.getId(),
                savedRule.getRuleCode(),
                savedRule.getRuleName(),
                savedRule.getQualificationCategory(),
                savedRule.getInterchangeRate()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/getRule")
    private RuleRequestDto getRule(
            @RequestParam UUID id
    ) {
        //todo
        return null;
    }

    @GetMapping("/getRules")
    private List<RuleRequestDto> getRules() {
        //todo
        return List.of(null);
    }

    @PostMapping("/delete")
    private ResponseEntity<String> deleteRule(
            @RequestParam UUID id
    ) {
        //todo
        return ResponseEntity.ok(
                "deleted"
        );
    }
}
