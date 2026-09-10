package org.schemeguard.backend.repository;

import jakarta.persistence.LockModeType;
import org.schemeguard.backend.entity.UploadSession;
import org.schemeguard.backend.entity.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadSessionRepository extends JpaRepository<UploadSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from UploadSession session where session.id = :id")
    Optional<UploadSession> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select session from UploadSession session
            where session.status in :statuses and session.updatedAt < :cutoff
            """)
    List<UploadSession> findStaleSessions(
            @Param("statuses") List<UploadStatus> statuses,
            @Param("cutoff") Instant cutoff
    );
}
