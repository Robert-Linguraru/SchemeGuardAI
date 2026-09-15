package org.schemeguard.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.schemeguard.backend.entity.enums.Region;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "country_regions")
public class CountryRegionNomenclator {

    @Id
    @Column(name = "merchant_country", length = 2, nullable = false)
    private String merchant_country;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false, length = 10)
    private Region region;
}
