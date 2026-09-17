package org.schemeguard.backend.service.ruleIntepreter;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.entity.*;
import org.schemeguard.backend.entity.enums.QualificationStatus;
import org.schemeguard.backend.entity.enums.TransactionStatus;
import org.schemeguard.backend.exception.ConflictException;
import org.schemeguard.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QualificationBatchService {
    private final TransactionRepository transactions;
    private final RuleRepository rules;
    private final QualificationResultRepository results;
    private final FeeCalculationRepository fees;
    private final CountryToRegionService countries;
    private final FallbackRateService fallbackRates;
    private final ConditionEvaluator evaluator;
    private final ObjectMapper mapper;

    public record BatchResult(int processed, int qualified, int fallback) {}

    @Transactional
    public BatchResult evaluateBatch(Instant cutoff, int batchSize) {
        var batch = transactions.lockUnevaluated(cutoff, batchSize);
        int qualified = 0;
        for (Transaction transaction : batch) {
            if (evaluate(transaction)) qualified++;
        }
        return new BatchResult(batch.size(), qualified, batch.size() - qualified);
    }

    private boolean evaluate(Transaction transaction) {
        String region = countries.getRegion(transaction.getMerchantCountry());
        var candidates = rules.filterApplicableRules(transaction.getScheme().getId(),
                                                        region,
                                                        transaction.getAuthorizedAt().atZone(ZoneOffset.UTC).toLocalDate());
        List<Map<String, Object>> passed = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();
        Rule selected = null;
        for (Rule candidate : candidates) {
            ConditionEvaluator.Evaluation evaluation;
            try {
                evaluation = evaluator.evaluate(transaction, candidate.getConditions());
            } catch (IllegalArgumentException ex) {
                throw new ConflictException("Invalid rule " + candidate.getId() + ": " + ex.getMessage());
            }
            appendTrace(passed, candidate, evaluation.passed());
            appendTrace(failed, candidate, evaluation.failed());
            if (evaluation.matches()) {
                selected = candidate;
                break;
            }
        }
        BigDecimal rate = selected == null ? fallbackRates.getRate(transaction.getScheme().getId(), region) : selected.getInterchangeRate();
        QualificationResult result = new QualificationResult();
        result.setTransaction(transaction);
        result.setRule(selected);
        result.setRuleVersion(selected == null ? null : selected.getVersion());
        result.setQualificationStatus(selected == null ? QualificationStatus.NOT_QUALIFIED : QualificationStatus.QUALIFIED);
        result.setQualificationCategory(selected == null ? "FALLBACK" : selected.getQualificationCategory());
        result.setPassedConditions(mapper.valueToTree(passed));
        result.setFailedConditions(mapper.valueToTree(failed));
        result.setExplanation(selected == null
                ? "No applicable rule satisfied every condition in merchant region " + region + "; fallback rate " + rate + " applied."
                : "Rule " + selected.getRuleCode() + " version " + selected.getVersion()
                    + " satisfied every condition; selected by priority, creation time, then rate.");
        results.save(result);

        FeeCalculation fee = new FeeCalculation();
        fee.setQualificationResult(result);
        fee.setInterchangeRate(rate);
        fee.setInterchangeFee(transaction.getAmount().multiply(rate).setScale(4, RoundingMode.HALF_UP));
        fee.setTotalFee(fee.getInterchangeFee());
        fees.save(fee);
        transaction.setStatus(TransactionStatus.PROCESSED);
        return selected != null;
    }

    private void appendTrace(List<Map<String, Object>> target, Rule rule, List<Map<String, Object>> conditions) {
        for (var condition : conditions) {
            Map<String, Object> trace = new LinkedHashMap<>(condition);
            trace.put("ruleId", rule.getId().toString());
            trace.put("ruleVersion", rule.getVersion());
            target.add(trace);
        }
    }
}
