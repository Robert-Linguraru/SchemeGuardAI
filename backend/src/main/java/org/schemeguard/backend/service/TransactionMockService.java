package org.schemeguard.backend.service;

import org.schemeguard.backend.dto.TransactionMockResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionMockService {

    private final List<TransactionMockResponse> transactions = List.of(
            new TransactionMockResponse(
                    UUID.fromString(
                            "11111111-1111-1111-1111-111111111111"
                    ),
                    "TX-MOCK-001",
                    "Example Electronics",
                    "VISA",
                    new BigDecimal("100.00"),
                    "EUR",
                    "CREDIT",
                    "CONSUMER",
                    "ECOMMERCE",
                    "FR",
                    "RO",
                    "PROCESSED",
                    "QUALIFIED",
                    new BigDecimal("0.30"),
                    "Consumer credit transaction authenticated with 3DS.",
                    OffsetDateTime.now().minusDays(1)
            ),
            new TransactionMockResponse(
                    UUID.fromString(
                            "22222222-2222-2222-2222-222222222222"
                    ),
                    "TX-MOCK-002",
                    "Demo Market",
                    "MASTERCARD",
                    new BigDecimal("250.50"),
                    "EUR",
                    "DEBIT",
                    "CONSUMER",
                    "POS",
                    "RO",
                    "RO",
                    "PROCESSED",
                    "NOT_QUALIFIED",
                    new BigDecimal("4.50"),
                    "Merchant data is incomplete.",
                    OffsetDateTime.now().minusDays(2)
            )
    );

    public List<TransactionMockResponse> findAll() {
        return transactions;
    }

    public TransactionMockResponse findById(UUID id) {
        return transactions.stream()
                .filter(transaction -> transaction.id().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transaction not found"
                        )
                );
    }
}