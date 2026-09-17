package org.schemeguard.backend.repository;

import org.schemeguard.backend.entity.enums.QualificationStatus;
import java.time.Instant;
import java.util.UUID;

public interface AnalyticsQualificationProjection {
    UUID getTransactionId();
    UUID getResultId();
    QualificationStatus getQualificationStatus();
    Instant getEvaluatedAt();
}
