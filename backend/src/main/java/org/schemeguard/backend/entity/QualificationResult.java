package org.schemeguard.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.schemeguard.backend.entity.enums.QualificationStatus;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "qualification_results")
public class QualificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private Rule rule;

    @Column(name = "rule_version")
    private Integer ruleVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification_status", nullable = false, length = 30)
    private QualificationStatus qualificationStatus;

    @Column(name = "qualification_category", length = 100)
    private String qualificationCategory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "passed_conditions", nullable = false, columnDefinition = "jsonb")
    private JsonNode passedConditions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "failed_conditions", nullable = false, columnDefinition = "jsonb")
    private JsonNode failedConditions;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @PrePersist
    protected void onCreate() {
        evaluatedAt = Instant.now();
    }
}