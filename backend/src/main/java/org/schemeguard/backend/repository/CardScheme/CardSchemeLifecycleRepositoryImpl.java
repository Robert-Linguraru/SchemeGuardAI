package org.schemeguard.backend.repository.CardScheme;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class CardSchemeLifecycleRepositoryImpl implements CardSchemeLifecycleRepository {

    private final JdbcTemplate jdbcTemplate;

    public CardSchemeLifecycleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void activateCascade(UUID cardSchemeId) {
        jdbcTemplate.update(
                "UPDATE card_schemes SET active = TRUE WHERE id = ?",
                cardSchemeId
        );
        jdbcTemplate.update(
                "UPDATE interchange_rules SET active = TRUE WHERE scheme_id = ?",
                cardSchemeId
        );
        jdbcTemplate.update(
                "UPDATE transactions SET active = TRUE WHERE scheme_id = ?",
                cardSchemeId
        );
        jdbcTemplate.update("""
                UPDATE qualification_results qr
                SET active = TRUE
                WHERE EXISTS (
                    SELECT 1
                    FROM transactions t
                    WHERE t.id = qr.transaction_id
                      AND t.scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update("""
                UPDATE fee_calculations fc
                SET active = TRUE
                WHERE EXISTS (
                    SELECT 1
                    FROM qualification_results qr
                    JOIN transactions t ON t.id = qr.transaction_id
                    WHERE qr.id = fc.qualification_result_id
                      AND t.scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update("""
                UPDATE ml_predictions mp
                SET active = TRUE
                WHERE EXISTS (
                    SELECT 1
                    FROM transactions t
                    WHERE t.id = mp.transaction_id
                      AND t.scheme_id = ?
                )
                """, cardSchemeId);
    }

    @Override
    public void deactivateCascade(UUID cardSchemeId) {
        jdbcTemplate.update(
                "UPDATE card_schemes SET active = FALSE WHERE id = ?",
                cardSchemeId
        );
        jdbcTemplate.update(
                "UPDATE interchange_rules SET active = FALSE WHERE scheme_id = ?",
                cardSchemeId
        );
        jdbcTemplate.update(
                "UPDATE transactions SET active = FALSE WHERE scheme_id = ?",
                cardSchemeId
        );
        jdbcTemplate.update("""
                UPDATE qualification_results qr
                SET active = FALSE
                WHERE EXISTS (
                    SELECT 1
                    FROM transactions t
                    WHERE t.id = qr.transaction_id
                      AND t.scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update("""
                UPDATE fee_calculations fc
                SET active = FALSE
                WHERE EXISTS (
                    SELECT 1
                    FROM qualification_results qr
                    JOIN transactions t ON t.id = qr.transaction_id
                    WHERE qr.id = fc.qualification_result_id
                      AND t.scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update("""
                UPDATE ml_predictions mp
                SET active = FALSE
                WHERE EXISTS (
                    SELECT 1
                    FROM transactions t
                    WHERE t.id = mp.transaction_id
                      AND t.scheme_id = ?
                )
                """, cardSchemeId);
    }

    @Override
    public void deleteCascade(UUID cardSchemeId) {
        jdbcTemplate.update("""
                DELETE FROM fee_calculations
                WHERE qualification_result_id IN (
                    SELECT qr.id
                    FROM qualification_results qr
                    JOIN transactions t ON t.id = qr.transaction_id
                    WHERE t.scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update("""
                DELETE FROM qualification_results
                WHERE transaction_id IN (
                    SELECT id FROM transactions WHERE scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update("""
                DELETE FROM ml_predictions
                WHERE transaction_id IN (
                    SELECT id FROM transactions WHERE scheme_id = ?
                )
                """, cardSchemeId);
        jdbcTemplate.update(
                "DELETE FROM transactions WHERE scheme_id = ?",
                cardSchemeId
        );
        jdbcTemplate.update(
                "DELETE FROM interchange_rules WHERE scheme_id = ?",
                cardSchemeId
        );
        jdbcTemplate.update(
                "DELETE FROM card_schemes WHERE id = ?",
                cardSchemeId
        );
    }
}
