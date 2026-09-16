package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.QualificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface QualificationResultRepository
        extends JpaRepository<QualificationResult, UUID> {

    @Query("""
        select qr
        from QualificationResult qr
        join fetch qr.transaction t
        left join fetch t.merchant
        left join fetch t.scheme
        left join fetch qr.rule r
        left join fetch r.scheme
        where t.id = :transactionId
        order by qr.evaluatedAt desc
    """)
    Optional<QualificationResult> findLatestByTransactionId(
            @Param("transactionId") UUID transactionId
    );
}