package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.QualificationResult;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface QualificationResultRepository extends CrudRepository<QualificationResult, UUID> {}
