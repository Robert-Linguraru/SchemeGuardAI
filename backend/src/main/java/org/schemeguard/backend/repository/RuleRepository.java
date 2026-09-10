package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Rule;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RuleRepository extends CrudRepository<Rule, UUID> {

}
