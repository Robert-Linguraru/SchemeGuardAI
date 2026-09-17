package org.schemeguard.backend.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface AnalyticsTransactionProjection {
    UUID getId();
    String getMerchantName();
    String getSchemeCode();
    BigDecimal getAmount();
    String getMerchantCountry();
    Instant getAuthorizedAt();
    String getStatus();
}
