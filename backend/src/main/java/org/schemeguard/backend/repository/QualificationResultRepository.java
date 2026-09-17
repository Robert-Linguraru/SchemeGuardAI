package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.QualificationResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface QualificationResultRepository extends CrudRepository<QualificationResult, UUID> {
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

    @Query("select qr from QualificationResult qr join fetch qr.transaction t order by qr.evaluatedAt desc")
    List<QualificationResult> findAllByOrderByEvaluatedAtDesc();

    @Query(value = """
            select qr.transaction_id as transactionId, qr.id as resultId,
                   qr.qualification_status as qualificationStatus,
                   qr.evaluated_at as evaluatedAt
            from qualification_results qr
            order by qr.evaluated_at desc
            """, nativeQuery = true)
    List<AnalyticsQualificationProjection> findAllForAnalytics();
}
