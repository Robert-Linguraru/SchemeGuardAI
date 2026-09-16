package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.FeeCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeeCalculationRepository
        extends JpaRepository<FeeCalculation, UUID> {

    Optional<FeeCalculation> findByQualificationResultId(
            UUID qualificationResultId
    );
}