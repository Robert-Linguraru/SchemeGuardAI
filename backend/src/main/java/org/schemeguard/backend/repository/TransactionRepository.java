package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
    @Query(value = """
            SELECT * FROM transactions WHERE status = 'NEW' AND created_at <= :cutoff
            ORDER BY created_at, id LIMIT :batchSize FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Transaction> lockUnevaluated(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
