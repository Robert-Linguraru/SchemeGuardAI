package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.CountryRegionNomenclator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CountryRegionRepository
        extends CrudRepository<CountryRegionNomenclator, String> {

    @Query("""
            SELECT c
            FROM CountryRegionNomenclator c
            WHERE c.merchant_country = :country
            """)
    Optional<CountryRegionNomenclator> getRegion(
            @Param("country") String country
    );
}