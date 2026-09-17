package org.schemeguard.backend.service.ruleIntepreter;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.exception.ConflictException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FallbackRateService {
    private final JdbcTemplate jdbc;

    public BigDecimal getRate(UUID schemeId, String region) {
        var rates = jdbc.queryForList("""
                SELECT interchange_rate FROM fallback_interchange_rates
                WHERE active AND (scheme_id = ? OR scheme_id IS NULL)
                AND (region = ? OR region = 'ALL')
                ORDER BY (scheme_id IS NOT NULL) DESC, (region <> 'ALL') DESC
                LIMIT 1
                """, BigDecimal.class, schemeId, region);
        if (rates.isEmpty()) {
            throw new ConflictException("No fallback interchange rate configured for scheme "
                    + schemeId + " and region " + region + ". Configure fallback_interchange_rates and retry.");
        }
        return rates.getFirst();
    }
}
