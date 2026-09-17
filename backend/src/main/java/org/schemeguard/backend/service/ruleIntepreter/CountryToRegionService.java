package org.schemeguard.backend.service.ruleIntepreter;

import org.schemeguard.backend.entity.CountryRegionNomenclator;
import org.schemeguard.backend.repository.CountryRegionRepository;
import org.springframework.stereotype.Service;

@Service
public class CountryToRegionService {

    private final CountryRegionRepository countryRegionRepository;

    public CountryToRegionService(CountryRegionRepository countryRegionRepository) {
        this.countryRegionRepository = countryRegionRepository;
    }

    public String getRegion(String country) {
        if (country == null) return "OTHER";
        return countryRegionRepository.getRegion(country.trim().toUpperCase(java.util.Locale.ROOT))
                .map(CountryRegionNomenclator::getRegion)
                .map(Enum::name)
                .orElse("OTHER");
    }
}
