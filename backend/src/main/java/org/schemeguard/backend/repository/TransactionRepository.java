package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByExternalId(String externalId);
}