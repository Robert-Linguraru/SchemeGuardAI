package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.UploadPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadPartRepository extends JpaRepository<UploadPart, Long> {

    @Query("""
            select part from UploadPart part
            where part.uploadSession.id = :uploadId and part.partNumber = :partNumber
            """)
    Optional<UploadPart> findByUploadIdAndPartNumber(
            @Param("uploadId") UUID uploadId,
            @Param("partNumber") int partNumber
    );

    @Query("""
            select part from UploadPart part
            where part.uploadSession.id = :uploadId
            order by part.partNumber
            """)
    List<UploadPart> findAllByUploadIdOrderByPartNumber(@Param("uploadId") UUID uploadId);
}
