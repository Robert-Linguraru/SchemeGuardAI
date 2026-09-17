package org.schemeguard.backend.service.ruleIntepreter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RuleEngine {
    private final QualificationBatchService batches;
    public record EvaluationSummary(long processed, long qualified, long fallback) {}

    public EvaluationSummary evaluateUnevaluated() {
        Instant cutoff = Instant.now();
        long processed = 0, qualified = 0, fallback = 0;
        while (true) {
            var batch = batches.evaluateBatch(cutoff, 100);
            processed += batch.processed();
            qualified += batch.qualified();
            fallback += batch.fallback();
            if (batch.processed() == 0) return new EvaluationSummary(processed, qualified, fallback);
        }
    }
}
