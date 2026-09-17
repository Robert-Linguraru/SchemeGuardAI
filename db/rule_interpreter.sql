CREATE TABLE IF NOT EXISTS fallback_interchange_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scheme_id UUID REFERENCES card_schemes(id),
    region VARCHAR(10) NOT NULL DEFAULT 'ALL'
        CHECK (region IN ('EU', 'US', 'UK', 'OTHER', 'ALL')),
    interchange_rate NUMERIC(8,5) NOT NULL CHECK (interchange_rate >= 0),
    active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE NULLS NOT DISTINCT (scheme_id, region)
);

ALTER TABLE country_regions DROP CONSTRAINT IF EXISTS chk_country_regions_region;
ALTER TABLE country_regions ADD CONSTRAINT chk_country_regions_region
    CHECK (region IN ('EU', 'US', 'UK', 'OTHER'));

CREATE INDEX IF NOT EXISTS idx_transactions_unevaluated
    ON transactions (created_at, id) WHERE status = 'NEW';
CREATE INDEX IF NOT EXISTS idx_qualification_transaction
    ON qualification_results (transaction_id, evaluated_at);