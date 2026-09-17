package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Rule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RuleRepository extends CrudRepository<Rule, UUID> {

    boolean existsBySchemeIdAndRuleCodeAndVersion(UUID schemeId, String ruleCode, int version);

    @EntityGraph(attributePaths = "scheme")
    @Query("""
            SELECT r FROM Rule r WHERE r.scheme.id = :schemeId
            AND r.region = :region AND r.active = true
            AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date)
            ORDER BY r.priority ASC, r.createdAt DESC, r.interchangeRate ASC, r.id ASC
            """)
    java.util.List<Rule> filterApplicableRules(
            @Param("schemeId") UUID schemeId, @Param("region") String region,
            @Param("date") java.time.LocalDate date);

    @EntityGraph(attributePaths = "scheme")
    Optional<Rule> findById(UUID id);

    @EntityGraph(attributePaths = "scheme")
    Iterable<Rule> findAll();
}
