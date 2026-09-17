-- Idempotent migration for databases created before card scheme lifecycle support.
-- The Docker PostgreSQL init process runs this after create_schema.sql.

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE qualification_results
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE fee_calculations
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE ml_predictions
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX IF NOT EXISTS idx_transactions_active ON transactions(active);
CREATE INDEX IF NOT EXISTS idx_qualification_active ON qualification_results(active);
CREATE INDEX IF NOT EXISTS idx_fee_calculations_active ON fee_calculations(active);
CREATE INDEX IF NOT EXISTS idx_ml_predictions_active ON ml_predictions(active);
