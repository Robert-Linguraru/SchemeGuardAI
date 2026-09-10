package org.schemeguard.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.schemeguard.backend.entity.enums.CardCategory;
import org.schemeguard.backend.entity.enums.CardType;
import org.schemeguard.backend.entity.enums.TransactionChannel;
import org.schemeguard.backend.entity.enums.TransactionStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true, length = 100)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id", nullable = false)
    private CardScheme scheme;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_category", nullable = false, length = 20)
    private CardCategory cardCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionChannel channel;

    @Column(name = "issuer_country", nullable = false, length = 2)
    private String issuerCountry;

    @Column(name = "merchant_country", nullable = false, length = 2)
    private String merchantCountry;

    @Column(name = "authorized_at", nullable = false)
    private Instant authorizedAt;

    @Column(name = "cleared_at")
    private Instant clearedAt;

    @Column(name = "three_ds_used", nullable = false)
    private Boolean threeDsUsed = false;

    @Column(name = "cvv_present", nullable = false)
    private Boolean cvvPresent = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.NEW;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", nullable = false, columnDefinition = "jsonb")
    private JsonNode rawData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();

        if (rawData == null) {
            rawData = JsonNodeFactory.instance.objectNode();
        }
    }
}