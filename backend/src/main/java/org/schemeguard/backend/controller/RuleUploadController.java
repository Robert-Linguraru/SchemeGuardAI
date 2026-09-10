package org.schemeguard.backend.controller;

import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/rule")
public class RuleUploadController {

    private final Logger logger = Logger.getLogger(RuleUploadController.class.getName());

    @PostMapping("/upload")
    private RuleRequestDto uploadRule(
            @RequestBody RuleRequestDto ruleRequestDto
    ) {
        return ruleRequestDto;
    }
}
