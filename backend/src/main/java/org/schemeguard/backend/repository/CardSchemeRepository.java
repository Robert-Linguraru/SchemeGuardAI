package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.CardScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardSchemeRepository extends JpaRepository<CardScheme, UUID> {
}
