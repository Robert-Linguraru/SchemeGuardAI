package org.schemeguard.backend.service.ruleIntepreter;

import org.schemeguard.backend.entity.CountryRegionNomenclator;
import org.schemeguard.backend.repository.CountryRegionRepository;
import org.springframework.stereotype.Service;

@Service
public class CountryToRegion {

    private final CountryRegionRepository countryRegionRepository;

    public CountryToRegion(CountryRegionRepository countryRegionRepository) {
        this.countryRegionRepository = countryRegionRepository;
    }

    public String getRegion(String country) {
        return countryRegionRepository.getRegion(country)
                .map(CountryRegionNomenclator::getRegion)
                .map(Enum::name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No region mapping found for country: " + country
                ));
    }
}
