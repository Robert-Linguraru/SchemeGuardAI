package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.FeeCalculation;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface FeeCalculationRepository extends CrudRepository<FeeCalculation, UUID> {}
