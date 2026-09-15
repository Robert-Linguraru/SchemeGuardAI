package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.CountryRegionNomenclator;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CountryRegionRepository extends CrudRepository<CountryRegionNomenclator, String> {

    @EntityGraph(attributePaths = "scheme")
    @Query("SELECT c FROM CountryToRegionNomenclator c WHERE c.merchant_country = :country")
    Optional<CountryRegionNomenclator> getRegion(String country);
}
