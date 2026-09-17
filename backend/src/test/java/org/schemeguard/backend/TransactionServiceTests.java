package org.schemeguard.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.entity.Merchant;
import org.schemeguard.backend.entity.QualificationResult;
import org.schemeguard.backend.entity.Transaction;
import org.schemeguard.backend.entity.enums.CardCategory;
import org.schemeguard.backend.entity.enums.CardType;
import org.schemeguard.backend.entity.enums.QualificationStatus;
import org.schemeguard.backend.entity.enums.TransactionChannel;
import org.schemeguard.backend.entity.enums.TransactionStatus;
import org.schemeguard.backend.dto.TransactionPageResponse;
import org.schemeguard.backend.dto.TransactionResponse;
import org.schemeguard.backend.dto.TransactionStatisticsResponse;
import org.schemeguard.backend.repository.QualificationResultRepository;
import org.schemeguard.backend.repository.TransactionRepository;
import org.schemeguard.backend.service.TransactionService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTests {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final QualificationResultRepository qualificationResultRepository = mock(QualificationResultRepository.class);
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                qualificationResultRepository
        );
        when(qualificationResultRepository.findForTransactions(any()))
                .thenReturn(List.of());
    }

    @Test
    void emptyTransactionResultIsReturnedAsEmptyPage() {
        when(transactionRepository.findAllByOrderByCreatedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        TransactionPageResponse response = transactionService.findTransactions(0, 50);

        assertEquals(List.of(), response.content());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
    }

    @Test
    void paginatedResultUsesRequestedPageAndSize() {
        Transaction transaction = transaction("TX-1");
        when(transactionRepository.findAllByOrderByCreatedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction), Pageable.ofSize(2), 5));

        TransactionPageResponse response = transactionService.findTransactions(1, 2);

        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals("TX-1", response.content().getFirst().externalId());
    }

    @Test
    void mapsTransactionFieldsAndNullQualificationSafely() {
        Transaction transaction = transaction("TX-1");
        when(transactionRepository.findAllByOrderByCreatedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));

        TransactionResponse response = transactionService.findTransactions(0, 50)
                .content().getFirst();

        assertEquals(transaction.getId(), response.id());
        assertEquals("Merchant One", response.merchantName());
        assertEquals("VISA", response.schemeCode());
        assertEquals("Visa", response.schemeName());
        assertEquals(new BigDecimal("12.50"), response.amount());
        assertEquals("PROCESSED", response.transactionStatus());
        assertNull(response.qualificationStatus());
        assertNull(response.qualificationCategory());
        assertNull(response.qualificationExplanation());
        assertNull(response.evaluatedAt());
    }

    @Test
    void mapsQualificationResultWhenPresent() {
        Transaction transaction = transaction("TX-1");
        QualificationResult result = new QualificationResult();
        result.setId(UUID.randomUUID());
        result.setTransaction(transaction);
        result.setQualificationStatus(QualificationStatus.QUALIFIED);
        result.setQualificationCategory("EU_POS");
        result.setExplanation("All conditions passed.");
        result.setEvaluatedAt(Instant.parse("2026-09-15T10:00:00Z"));
        when(transactionRepository.findAllByOrderByCreatedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));
        when(qualificationResultRepository.findForTransactions(any()))
                .thenReturn(List.of(result));

        TransactionResponse response = transactionService.findTransactions(0, 50)
                .content().getFirst();

        assertEquals("QUALIFIED", response.qualificationStatus());
        assertEquals("EU_POS", response.qualificationCategory());
        assertEquals("All conditions passed.", response.qualificationExplanation());
        assertEquals(result.getEvaluatedAt(), response.evaluatedAt());
    }

    @Test
    void normalizesNegativeAndOversizedPagination() {
        when(transactionRepository.findAllByOrderByCreatedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        TransactionPageResponse negativePage = transactionService.findTransactions(-2, -1);
        TransactionPageResponse oversizedPage = transactionService.findTransactions(0, 500);

        assertEquals(0, negativePage.page());
        assertEquals(50, negativePage.size());
        assertEquals(100, oversizedPage.size());
        verify(transactionRepository, org.mockito.Mockito.times(2))
                .findAllByOrderByCreatedAtDescIdDesc(any(Pageable.class));
    }

    @Test
    void zeroTransactionStatisticsAreReturned() {
        when(transactionRepository.count()).thenReturn(0L);
        when(transactionRepository.sumAmount()).thenReturn(BigDecimal.ZERO);
        when(qualificationResultRepository.countQualifiedTransactions()).thenReturn(0L);
        when(qualificationResultRepository.countNotQualifiedTransactions()).thenReturn(0L);
        when(qualificationResultRepository.countPartiallyQualifiedTransactions()).thenReturn(0L);
        when(qualificationResultRepository.countTransactionsWithoutResult()).thenReturn(0L);

        TransactionStatisticsResponse response = transactionService.getStatistics();

        assertEquals(0, response.totalTransactionCount());
        assertEquals(BigDecimal.ZERO, response.totalTransactionAmount());
        assertEquals(0, response.withoutQualificationResultCount());
    }

    @Test
    void statisticsIncludeQualificationCountsAndAmount() {
        when(transactionRepository.count()).thenReturn(3L);
        when(transactionRepository.sumAmount()).thenReturn(new BigDecimal("35.00"));
        when(qualificationResultRepository.countQualifiedTransactions()).thenReturn(1L);
        when(qualificationResultRepository.countNotQualifiedTransactions()).thenReturn(1L);
        when(qualificationResultRepository.countPartiallyQualifiedTransactions()).thenReturn(1L);
        when(qualificationResultRepository.countTransactionsWithoutResult()).thenReturn(0L);

        TransactionStatisticsResponse response = transactionService.getStatistics();

        assertEquals(3, response.totalTransactionCount());
        assertEquals(1, response.qualifiedCount());
        assertEquals(1, response.notQualifiedCount());
        assertEquals(1, response.partiallyQualifiedCount());
        assertEquals(new BigDecimal("35.00"), response.totalTransactionAmount());
    }

    private Transaction transaction(String externalId) {
        Merchant merchant = new Merchant();
        merchant.setName("Merchant One");
        CardScheme scheme = new CardScheme();
        scheme.setCode("VISA");
        scheme.setName("Visa");

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setExternalId(externalId);
        transaction.setMerchant(merchant);
        transaction.setScheme(scheme);
        transaction.setAmount(new BigDecimal("12.50"));
        transaction.setCurrencyCode("EUR");
        transaction.setCardType(CardType.CREDIT);
        transaction.setCardCategory(CardCategory.CONSUMER);
        transaction.setChannel(TransactionChannel.POS);
        transaction.setIssuerCountry("FR");
        transaction.setMerchantCountry("RO");
        transaction.setStatus(TransactionStatus.PROCESSED);
        transaction.setCreatedAt(Instant.parse("2026-09-15T09:00:00Z"));
        return transaction;
    }
}