package org.schemeguard.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.schemeguard.backend.entity.enums.RiskLevel;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ml_predictions")
public class MlPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "qualification_probability", precision = 6, scale = 5)
    private BigDecimal qualificationProbability;

    @Column(name = "risk_score", precision = 6, scale = 5)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "important_features", nullable = false, columnDefinition = "jsonb")
    private JsonNode importantFeatures;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "predicted_at", nullable = false)
    private Instant predictedAt;

    @PrePersist
    protected void onCreate() {
        predictedAt = Instant.now();
    }
}