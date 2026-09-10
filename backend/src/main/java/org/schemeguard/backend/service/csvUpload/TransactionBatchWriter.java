package org.schemeguard.backend.service.csvUpload;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TransactionBatchWriter {

    private static final String UPSERT_TRANSACTION_SQL = """
            INSERT INTO transactions (
                external_id, merchant_id, scheme_id, amount, currency_code,
                card_type, card_category, channel, issuer_country, merchant_country,
                authorized_at, cleared_at, three_ds_used, cvv_present, status, raw_data
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NEW', ?::jsonb)
            ON CONFLICT (external_id) DO UPDATE SET
                merchant_id = EXCLUDED.merchant_id,
                scheme_id = EXCLUDED.scheme_id,
                amount = EXCLUDED.amount,
                currency_code = EXCLUDED.currency_code,
                card_type = EXCLUDED.card_type,
                card_category = EXCLUDED.card_category,
                channel = EXCLUDED.channel,
                issuer_country = EXCLUDED.issuer_country,
                merchant_country = EXCLUDED.merchant_country,
                authorized_at = EXCLUDED.authorized_at,
                cleared_at = EXCLUDED.cleared_at,
                three_ds_used = EXCLUDED.three_ds_used,
                cvv_present = EXCLUDED.cvv_present,
                status = EXCLUDED.status,
                raw_data = EXCLUDED.raw_data
            """;

    private static final String UPSERT_MERCHANT_SQL = """
            INSERT INTO merchants (
                id, name, mcc, country_code, default_currency, status
            ) VALUES (?, ?, ?, ?, ?, 'ACTIVE')
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                mcc = EXCLUDED.mcc,
                country_code = EXCLUDED.country_code,
                default_currency = EXCLUDED.default_currency,
                status = EXCLUDED.status
            """;

    private static final String UPSERT_SCHEME_SQL = """
            INSERT INTO card_schemes (code, name, active)
            VALUES (?, ?, TRUE)
            ON CONFLICT (code) DO UPDATE SET
                name = EXCLUDED.name,
                active = TRUE
            RETURNING id
            """;

    private final JdbcTemplate jdbcTemplate;

    public TransactionBatchWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void write(UUID uploadId, List<TransactionRow> rows) {
        Map<String, UUID> merchantIds = new HashMap<>();
        Map<String, UUID> schemeIds = new HashMap<>();

        for (TransactionRow row : rows) {
            merchantIds.computeIfAbsent(row.merchantId(), ignored -> upsertMerchant(row));
            schemeIds.computeIfAbsent(row.network(), ignored -> upsertScheme(row.network()));
        }

        jdbcTemplate.batchUpdate(UPSERT_TRANSACTION_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws SQLException {
                TransactionRow row = rows.get(index);
                int parameter = 1;
                statement.setString(parameter++, row.transactionId());
                statement.setObject(parameter++, merchantIds.get(row.merchantId()));
                statement.setObject(parameter++, schemeIds.get(row.network()));
                statement.setBigDecimal(parameter++, row.amount());
                statement.setString(parameter++, row.currency());
                statement.setString(parameter++, row.targetCardType(index + 1L));
                statement.setString(parameter++, row.targetCardCategory());
                statement.setString(parameter++, row.targetChannel(index + 1L));
                statement.setString(parameter++, row.cardCountry());
                statement.setString(parameter++, row.merchantCountry());
                statement.setObject(parameter++, row.authorizationTimestamp());
                if (row.clearingTimestamp() == null) {
                    statement.setNull(parameter++, Types.TIMESTAMP_WITH_TIMEZONE);
                } else {
                    statement.setObject(parameter++, row.clearingTimestamp());
                }
                statement.setBoolean(parameter++, row.threeDsUsed());
                statement.setBoolean(parameter++, row.cvvPresent());
                statement.setString(parameter, row.rawDataJson());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    private UUID upsertMerchant(TransactionRow row) {
        UUID merchantId = UUID.nameUUIDFromBytes(
                ("schemeguard:merchant:" + row.merchantId())
                        .getBytes(StandardCharsets.UTF_8)
        );
        jdbcTemplate.update(
                UPSERT_MERCHANT_SQL,
                merchantId,
                row.merchantName(),
                row.mcc(),
                row.merchantCountry(),
                row.currency()
        );
        return merchantId;
    }

    private UUID upsertScheme(String network) {
        return jdbcTemplate.queryForObject(
                UPSERT_SCHEME_SQL,
                UUID.class,
                network,
                network
        );
    }
}
