package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, UUID> {
}
