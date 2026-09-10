package org.schemeguard.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fee_calculations")
public class FeeCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "qualification_result_id",
            nullable = false,
            unique = true
    )
    private QualificationResult qualificationResult;

    @Column(name = "interchange_rate", nullable = false, precision = 8, scale = 5)
    private BigDecimal interchangeRate;

    @Column(name = "interchange_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal interchangeFee;

    @Column(name = "scheme_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal schemeFee = BigDecimal.ZERO;

    @Column(name = "acquirer_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal acquirerFee = BigDecimal.ZERO;

    @Column(name = "total_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFee;

    @Column(name = "optimized_rate", precision = 8, scale = 5)
    private BigDecimal optimizedRate;

    @Column(name = "optimized_fee", precision = 19, scale = 4)
    private BigDecimal optimizedFee;

    @Column(name = "potential_saving", nullable = false, precision = 19, scale = 4)
    private BigDecimal potentialSaving = BigDecimal.ZERO;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = Instant.now();
    }
}