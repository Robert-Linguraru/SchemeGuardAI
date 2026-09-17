package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByExternalId(String externalId);

    @Query("select t from Transaction t join fetch t.merchant join fetch t.scheme")
    List<Transaction> findAllWithMerchantAndScheme();

    @Query(value = """
            select t.id as id, m.name as merchantName, cs.code as schemeCode,
                   t.amount as amount, t.merchant_country as merchantCountry,
                   t.authorized_at as authorizedAt, t.status as status
            from transactions t
            join merchants m on m.id = t.merchant_id
            join card_schemes cs on cs.id = t.scheme_id
            """, nativeQuery = true)
    List<AnalyticsTransactionProjection> findAllForAnalytics();
}
