package org.schemeguard.backend.repository.CardScheme;

import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.repository.CardScheme.CardSchemeLifecycleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CardSchemeRepository
        extends JpaRepository<CardScheme, UUID>, CardSchemeLifecycleRepository {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    @Query("""
            SELECT scheme
            FROM CardScheme scheme
            WHERE (:includeInactive = true OR scheme.active = true)
              AND (
                    :search = ''
                    OR LOWER(scheme.code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(scheme.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<CardScheme> search(
            @Param("includeInactive") boolean includeInactive,
            @Param("search") String search,
            Pageable pageable
    );
}
