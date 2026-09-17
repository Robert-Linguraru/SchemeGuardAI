package org.schemeguard.backend.service;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Analytics.AnalyticsMetricDto;
import org.schemeguard.backend.dto.Analytics.AnalyticsResponseDto;
import org.schemeguard.backend.entity.FeeCalculation;
import org.schemeguard.backend.entity.enums.QualificationStatus;
import org.schemeguard.backend.entity.enums.TransactionStatus;
import org.schemeguard.backend.repository.AnalyticsQualificationProjection;
import org.schemeguard.backend.repository.AnalyticsTransactionProjection;
import org.schemeguard.backend.repository.FeeCalculationRepository;
import org.schemeguard.backend.repository.QualificationResultRepository;
import org.schemeguard.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AnalyticsAggregationService {
    private final TransactionRepository transactionRepository;
    private final QualificationResultRepository qualificationResultRepository;
    private final FeeCalculationRepository feeCalculationRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponseDto aggregate(LocalDate from, LocalDate to, String scheme, String country, String merchant, QualificationStatus qualification, TransactionStatus status) {
        Instant fromInstant = from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Map<UUID, AnalyticsQualificationProjection> latestResults = new HashMap<>();
        qualificationResultRepository.findAllForAnalytics().forEach(result -> latestResults.putIfAbsent(result.getTransactionId(), result));
        List<AnalyticsTransactionProjection> filtered = transactionRepository.findAllForAnalytics().stream()
                .filter(transaction -> matches(transaction, fromInstant, toInstant, scheme, country, merchant, qualification, status, latestResults.get(transaction.getId())))
                .toList();
        Map<UUID, FeeCalculation> fees = new HashMap<>();
        latestResults.values().forEach(result -> feeCalculationRepository.findByQualificationResultId(result.getResultId()).ifPresent(fee -> fees.put(result.getTransactionId(), fee)));
        long qualified = countQualification(filtered, latestResults, QualificationStatus.QUALIFIED);
        long partiallyQualified = countQualification(filtered, latestResults, QualificationStatus.PARTIALLY_QUALIFIED);
        long notQualified = countQualification(filtered, latestResults, QualificationStatus.NOT_QUALIFIED);
        BigDecimal totalAmount = sum(filtered, AnalyticsTransactionProjection::getAmount);
        BigDecimal totalFees = filtered.stream().map(transaction -> feeValue(fees.get(transaction.getId()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AnalyticsResponseDto(filtered.size(), filtered.stream().filter(transaction -> TransactionStatus.PROCESSED.name().equals(transaction.getStatus())).count(), qualified, partiallyQualified, notQualified, totalAmount, totalFees,
                filtered.isEmpty() ? BigDecimal.ZERO : totalAmount.divide(BigDecimal.valueOf(filtered.size()), 2, RoundingMode.HALF_UP),
                filtered.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(qualified * 100.0 / filtered.size()).setScale(2, RoundingMode.HALF_UP),
                metrics(filtered, transaction -> qualificationName(latestResults.get(transaction.getId())), transaction -> BigDecimal.ZERO),
                metrics(filtered, AnalyticsTransactionProjection::getSchemeCode, transaction -> feeValue(fees.get(transaction.getId()))),
                metrics(filtered, AnalyticsTransactionProjection::getMerchantCountry, transaction -> BigDecimal.ZERO),
                metrics(filtered, AnalyticsTransactionProjection::getMerchantName, transaction -> feeValue(fees.get(transaction.getId()))));
    }

    private boolean matches(AnalyticsTransactionProjection transaction, Instant from, Instant to, String scheme, String country, String merchant, QualificationStatus qualification, TransactionStatus status, AnalyticsQualificationProjection result) {
        return (from == null || !transaction.getAuthorizedAt().isBefore(from)) && (to == null || transaction.getAuthorizedAt().isBefore(to)) &&
                (scheme == null || scheme.isBlank() || transaction.getSchemeCode().equalsIgnoreCase(scheme)) &&
                (country == null || country.isBlank() || transaction.getMerchantCountry().equalsIgnoreCase(country)) &&
                (merchant == null || merchant.isBlank() || transaction.getMerchantName().equalsIgnoreCase(merchant)) &&
                (status == null || transaction.getStatus().equals(status.name())) &&
                (qualification == null || (result != null && result.getQualificationStatus() == qualification));
    }

    private long countQualification(List<AnalyticsTransactionProjection> transactions, Map<UUID, AnalyticsQualificationProjection> results, QualificationStatus status) {
        return transactions.stream().filter(transaction -> results.containsKey(transaction.getId()) && results.get(transaction.getId()).getQualificationStatus() == status).count();
    }

    private String qualificationName(AnalyticsQualificationProjection result) {
        return result == null ? "PENDING" : result.getQualificationStatus().name();
    }

    private BigDecimal feeValue(FeeCalculation fee) {
        return fee == null || fee.getTotalFee() == null ? BigDecimal.ZERO : fee.getTotalFee();
    }

    private BigDecimal sum(List<AnalyticsTransactionProjection> transactions, Function<AnalyticsTransactionProjection, BigDecimal> value) {
        return transactions.stream().map(value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<AnalyticsMetricDto> metrics(List<AnalyticsTransactionProjection> transactions, Function<AnalyticsTransactionProjection, String> name, Function<AnalyticsTransactionProjection, BigDecimal> fee) {
        Map<String, Long> counts = new HashMap<>();
        Map<String, BigDecimal> fees = new HashMap<>();
        transactions.forEach(transaction -> {
            String key = name.apply(transaction);
            counts.merge(key, 1L, Long::sum);
            fees.merge(key, fee.apply(transaction), BigDecimal::add);
        });
        return counts.keySet().stream().map(key -> new AnalyticsMetricDto(key, counts.get(key), fees.getOrDefault(key, BigDecimal.ZERO))).sorted(Comparator.comparing(AnalyticsMetricDto::transactions).reversed()).toList();
    }
}
