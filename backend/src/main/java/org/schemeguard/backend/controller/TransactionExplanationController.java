package org.schemeguard.backend.controller;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Explanation.TransactionExplanationResponse;
import org.schemeguard.backend.dto.TransactionMockResponse;
import org.schemeguard.backend.service.LlmExplanationService;
import org.schemeguard.backend.service.TransactionMockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionExplanationController {

    private final TransactionMockService transactionMockService;
    private final LlmExplanationService llmExplanationService;

    @GetMapping("/{id}/explanation")
    public ResponseEntity<TransactionExplanationResponse> explain(
            @PathVariable UUID id
    ) {
        TransactionMockResponse transaction =
                transactionMockService.findById(id);

        String data = """
                External ID: %s
                Amount: %s %s
                Card type: %s
                Card category: %s
                Channel: %s
                Issuer country: %s
                Merchant country: %s
                Status: %s
                Qualification status: %s
                Estimated fee: %s %s
                Rule explanation: %s
                """.formatted(
                transaction.externalId(),
                transaction.amount(),
                transaction.currencyCode(),
                transaction.cardType(),
                transaction.cardCategory(),
                transaction.channel(),
                transaction.issuerCountry(),
                transaction.merchantCountry(),
                transaction.status(),
                transaction.qualificationStatus(),
                transaction.estimatedFee(),
                transaction.currencyCode(),
                transaction.explanation()
        );

        String llmExplanation = llmExplanationService.explain(data);

        return ResponseEntity.ok(
                new TransactionExplanationResponse(
                        transaction.id(),
                        transaction.externalId(),
                        transaction.qualificationStatus(),
                        transaction.explanation(),
                        llmExplanation,
                        transaction.estimatedFee(),
                        transaction.currencyCode()
                )
        );
    }
}