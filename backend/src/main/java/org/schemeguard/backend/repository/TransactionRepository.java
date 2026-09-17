package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @EntityGraph(attributePaths = {"merchant", "scheme"})
    Page<Transaction> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    @Query("select coalesce(sum(transaction.amount), 0) from Transaction transaction")
    BigDecimal sumAmount();
}