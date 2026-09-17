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

    //todo
    @EntityGraph(attributePaths = "scheme")
    //@Query("SELECT r FROM Rule r")
    @Query("""
            SELECT r
            FROM Rule r
            WHERE r.active = true
              AND r.scheme.active = true
              AND (:schemeId IS NULL OR r.scheme.id = :schemeId)
            """)
    Iterable<Rule> filterApplicableRules(
            @Param("schemeId") UUID schemeId
    );

    @EntityGraph(attributePaths = "scheme")
    Optional<Rule> findById(UUID id);

    @EntityGraph(attributePaths = "scheme")
    Iterable<Rule> findAll();
}
