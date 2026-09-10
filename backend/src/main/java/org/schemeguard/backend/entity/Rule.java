package org.schemeguard.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "interchange_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rule_scheme_code_version",
                        columnNames = {"scheme_id", "rule_code", "version"}
                )
        }
)
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id", nullable = false)
    private CardScheme scheme;

    @Column(name = "rule_code", nullable = false, length = 80)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(nullable = false, length = 10)
    private String region;

    @Column(nullable = false)
    private Integer priority = 100;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> conditions;

    @Column(name = "qualification_category", nullable = false, length = 100)
    private String qualificationCategory;

    @Column(name = "interchange_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal interchangeRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}