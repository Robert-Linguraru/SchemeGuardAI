package org.schemeguard.backend.service;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Explanation.TransactionExplanationResponse;
import org.schemeguard.backend.entity.FeeCalculation;
import org.schemeguard.backend.entity.QualificationResult;
import org.schemeguard.backend.entity.Transaction;
import org.schemeguard.backend.exception.ResourceNotFoundException;
import org.schemeguard.backend.repository.FeeCalculationRepository;
import org.schemeguard.backend.repository.QualificationResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionExplanationService {

    private final QualificationResultRepository qualificationResultRepository;
    private final FeeCalculationRepository feeCalculationRepository;
    private final LlmExplanationService llmExplanationService;

    @Transactional(readOnly = true)
    public TransactionExplanationResponse explain(UUID transactionId) {
        QualificationResult result =
                qualificationResultRepository
                        .findLatestByTransactionId(transactionId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No qualification result found for transaction"
                        ));

        Transaction transaction = result.getTransaction();

        FeeCalculation fee = feeCalculationRepository
                .findByQualificationResultId(result.getId())
                .orElse(null);

        String data = """
                Transaction ID: %s
                External ID: %s
                Amount: %s %s
                Card type: %s
                Card category: %s
                Channel: %s
                Issuer country: %s
                Merchant country: %s
                3DS used: %s
                Qualification status: %s
                Qualification category: %s
                Failed conditions: %s
                Passed conditions: %s
                Original explanation: %s
                Interchange rate: %s
                Interchange fee: %s
                """.formatted(
                transaction.getId(),
                transaction.getExternalId(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getCardType(),
                transaction.getCardCategory(),
                transaction.getChannel(),
                transaction.getIssuerCountry(),
                transaction.getMerchantCountry(),
                transaction.getThreeDsUsed(),
                result.getQualificationStatus(),
                result.getQualificationCategory(),
                result.getFailedConditions(),
                result.getPassedConditions(),
                result.getExplanation(),
                fee == null ? "not available" : fee.getInterchangeRate(),
                fee == null ? "not available" : fee.getInterchangeFee()
        );

        String llmExplanation = llmExplanationService.explain(data);

        return new TransactionExplanationResponse(
                transaction.getId(),
                transaction.getExternalId(),
                result.getQualificationStatus().name(),
                result.getQualificationCategory(),
                result.getExplanation(),
                llmExplanation,
                fee == null ? null : fee.getInterchangeRate(),
                fee == null ? null : fee.getInterchangeFee(),
                transaction.getCurrencyCode()
        );
    }
}