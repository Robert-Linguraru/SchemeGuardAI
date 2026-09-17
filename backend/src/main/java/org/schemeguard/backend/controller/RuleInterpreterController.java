package org.schemeguard.backend.controller;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.service.ruleIntepreter.RuleEngine;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qualifications")
@RequiredArgsConstructor
public class RuleInterpreterController {
    private final RuleEngine engine;

    @PostMapping("/evaluate")
    public RuleEngine.EvaluationSummary evaluateUnevaluated() {
        return engine.evaluateUnevaluated();
    }
}
