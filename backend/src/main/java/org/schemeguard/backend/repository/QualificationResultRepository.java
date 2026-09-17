package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.QualificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface QualificationResultRepository extends JpaRepository<QualificationResult, UUID> {

    @Query("""
            select result from QualificationResult result
            where result.transaction.id in :transactionIds
            order by result.evaluatedAt desc, result.id desc
            """)
    List<QualificationResult> findForTransactions(
            @Param("transactionIds") Collection<UUID> transactionIds
    );

                @Query("""
                                                select count(result) from QualificationResult result
                                                where result.qualificationStatus = org.schemeguard.backend.entity.enums.QualificationStatus.QUALIFIED
                                                        and not exists (
                                                                        select newer.id from QualificationResult newer
                                                                        where newer.transaction = result.transaction
                                                                                and (newer.evaluatedAt > result.evaluatedAt
                                                                                                 or (newer.evaluatedAt = result.evaluatedAt and newer.id > result.id))
                                                        )
                                                """)
    long countQualifiedTransactions();

                @Query("""
                                                select count(result) from QualificationResult result
                                                where result.qualificationStatus = org.schemeguard.backend.entity.enums.QualificationStatus.NOT_QUALIFIED
                                                        and not exists (
                                                                        select newer.id from QualificationResult newer
                                                                        where newer.transaction = result.transaction
                                                                                and (newer.evaluatedAt > result.evaluatedAt
                                                                                                 or (newer.evaluatedAt = result.evaluatedAt and newer.id > result.id))
                                                        )
                                                """)
    long countNotQualifiedTransactions();

                @Query("""
                                                select count(result) from QualificationResult result
                                                where result.qualificationStatus = org.schemeguard.backend.entity.enums.QualificationStatus.PARTIALLY_QUALIFIED
                                                        and not exists (
                                                                        select newer.id from QualificationResult newer
                                                                        where newer.transaction = result.transaction
                                                                                and (newer.evaluatedAt > result.evaluatedAt
                                                                                                 or (newer.evaluatedAt = result.evaluatedAt and newer.id > result.id))
                                                        )
                                                """)
    long countPartiallyQualifiedTransactions();

    @Query("select count(transaction) from Transaction transaction where not exists (select result.id from QualificationResult result where result.transaction = transaction)")
    long countTransactionsWithoutResult();
}
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
