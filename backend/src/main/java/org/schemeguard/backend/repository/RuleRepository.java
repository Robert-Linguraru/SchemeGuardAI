package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Rule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RuleRepository extends CrudRepository<Rule, UUID> {

    @Query("select r from Rule r where r.id = :id")
    Rule findRuleById(@Param("id") UUID id);
}
