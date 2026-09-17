package org.schemeguard.backend.service;

import org.schemeguard.backend.dto.TransactionPageResponse;
import org.schemeguard.backend.dto.TransactionResponse;
import org.schemeguard.backend.dto.TransactionStatisticsResponse;
import org.schemeguard.backend.entity.QualificationResult;
import org.schemeguard.backend.entity.Transaction;
import org.schemeguard.backend.repository.QualificationResultRepository;
import org.schemeguard.backend.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final QualificationResultRepository qualificationResultRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            QualificationResultRepository qualificationResultRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.qualificationResultRepository = qualificationResultRepository;
    }

    @Transactional(readOnly = true)
    public TransactionPageResponse findTransactions(int requestedPage, int requestedSize) {
        int page = Math.max(requestedPage, 0);
        int size = requestedSize <= 0
                ? DEFAULT_PAGE_SIZE
                : Math.min(requestedSize, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<Transaction> resultPage = transactionRepository
                .findAllByOrderByCreatedAtDescIdDesc(pageable);

        List<UUID> transactionIds = resultPage.getContent().stream()
                .map(Transaction::getId)
                .toList();
        Map<UUID, QualificationResult> qualifications = latestQualifications(transactionIds);
        List<TransactionResponse> content = resultPage.getContent().stream()
                .map(transaction -> toResponse(transaction, qualifications.get(transaction.getId())))
                .toList();

        return new TransactionPageResponse(
                content,
                page,
                size,
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public TransactionStatisticsResponse getStatistics() {
                BigDecimal totalAmount = transactionRepository.sumAmount();
        return new TransactionStatisticsResponse(
                transactionRepository.count(),
                qualificationResultRepository.countQualifiedTransactions(),
                qualificationResultRepository.countNotQualifiedTransactions(),
                qualificationResultRepository.countPartiallyQualifiedTransactions(),
                qualificationResultRepository.countTransactionsWithoutResult(),
                totalAmount == null
                        ? BigDecimal.ZERO
                        : totalAmount
        );
    }

    private Map<UUID, QualificationResult> latestQualifications(List<UUID> transactionIds) {
        if (transactionIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, QualificationResult> latest = new HashMap<>();
        qualificationResultRepository.findForTransactions(transactionIds)
                .forEach(result -> latest.putIfAbsent(result.getTransaction().getId(), result));
        return latest;
    }

    private TransactionResponse toResponse(
            Transaction transaction,
            QualificationResult qualification
    ) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getExternalId(),
                transaction.getMerchant().getName(),
                transaction.getScheme().getCode(),
                transaction.getScheme().getName(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getCardType() == null ? null : transaction.getCardType().name(),
                transaction.getCardCategory() == null ? null : transaction.getCardCategory().name(),
                transaction.getChannel() == null ? null : transaction.getChannel().name(),
                transaction.getIssuerCountry(),
                transaction.getMerchantCountry(),
                transaction.getStatus() == null ? null : transaction.getStatus().name(),
                qualification == null || qualification.getQualificationStatus() == null
                        ? null
                        : qualification.getQualificationStatus().name(),
                qualification == null ? null : qualification.getQualificationCategory(),
                qualification == null ? null : qualification.getExplanation(),
                qualification == null ? null : qualification.getEvaluatedAt(),
                transaction.getCreatedAt()
        );
    }
}