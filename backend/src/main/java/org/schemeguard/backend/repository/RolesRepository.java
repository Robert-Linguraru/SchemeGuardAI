package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RolesRepository extends CrudRepository<Role, UUID> {

    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Role findRoleByName(@Param("name") String name);
}
