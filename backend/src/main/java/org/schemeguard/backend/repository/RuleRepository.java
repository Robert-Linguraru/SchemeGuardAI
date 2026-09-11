package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Rule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RuleRepository extends CrudRepository<Rule, UUID> {

    @EntityGraph(attributePaths = "scheme")
    Optional<Rule> findById(UUID id);

    @EntityGraph(attributePaths = "scheme")
    Iterable<Rule> findAll();
}
