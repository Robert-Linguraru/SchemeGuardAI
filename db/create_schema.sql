CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE merchants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    mcc VARCHAR(4) NOT NULL CHECK (mcc ~ '^[0-9]{4}$'),
    country_code CHAR(2) NOT NULL CHECK (country_code ~ '^[A-Z]{2}$'),
    default_currency CHAR(3) NOT NULL CHECK (default_currency ~ '^[A-Z]{3}$'),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE merchant_users (
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (merchant_id, user_id)
);

CREATE TABLE card_schemes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(100) NOT NULL UNIQUE,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    scheme_id UUID NOT NULL REFERENCES card_schemes(id),
    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency_code CHAR(3) NOT NULL CHECK (currency_code ~ '^[A-Z]{3}$'),
    card_type VARCHAR(20) NOT NULL CHECK (card_type IN ('DEBIT', 'CREDIT', 'COMMERCIAL')),
    card_category VARCHAR(20) NOT NULL CHECK (card_category IN ('CONSUMER', 'BUSINESS')),
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('POS', 'ECOMMERCE', 'MOTO')),
    issuer_country CHAR(2) NOT NULL CHECK (issuer_country ~ '^[A-Z]{2}$'),
    merchant_country CHAR(2) NOT NULL CHECK (merchant_country ~ '^[A-Z]{2}$'),
    authorized_at TIMESTAMPTZ NOT NULL,
    cleared_at TIMESTAMPTZ,
    three_ds_used BOOLEAN NOT NULL DEFAULT false,
    cvv_present BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'PROCESSED', 'FAILED')),
    raw_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT cleared_after_authorization CHECK (cleared_at IS NULL OR cleared_at >= authorized_at)
);

CREATE TABLE interchange_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scheme_id UUID NOT NULL REFERENCES card_schemes(id),
    rule_code VARCHAR(80) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    region VARCHAR(10) NOT NULL CHECK (region IN ('EU', 'US', 'UK')),
    priority INTEGER NOT NULL DEFAULT 100 CHECK (priority >= 0),
    conditions JSONB NOT NULL,
    qualification_category VARCHAR(100) NOT NULL,
    interchange_rate NUMERIC(8,5) NOT NULL CHECK (interchange_rate >= 0),
    effective_from DATE NOT NULL,
    effective_to DATE,
    version INTEGER NOT NULL DEFAULT 1 CHECK (version > 0),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (scheme_id, rule_code, version),
    CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE qualification_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    rule_id UUID REFERENCES interchange_rules(id),
    rule_version INTEGER,
    qualification_status VARCHAR(30) NOT NULL CHECK (qualification_status IN ('QUALIFIED', 'PARTIALLY_QUALIFIED', 'NOT_QUALIFIED')),
    qualification_category VARCHAR(100),
    passed_conditions JSONB NOT NULL DEFAULT '[]'::jsonb,
    failed_conditions JSONB NOT NULL DEFAULT '[]'::jsonb,
    explanation TEXT NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE fee_calculations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    qualification_result_id UUID NOT NULL UNIQUE REFERENCES qualification_results(id) ON DELETE CASCADE,
    interchange_rate NUMERIC(8,5) NOT NULL CHECK (interchange_rate >= 0),
    interchange_fee NUMERIC(19,4) NOT NULL CHECK (interchange_fee >= 0),
    scheme_fee NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (scheme_fee >= 0),
    acquirer_fee NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (acquirer_fee >= 0),
    total_fee NUMERIC(19,4) NOT NULL CHECK (total_fee >= 0),
    optimized_rate NUMERIC(8,5) CHECK (optimized_rate IS NULL OR optimized_rate >= 0),
    optimized_fee NUMERIC(19,4) CHECK (optimized_fee IS NULL OR optimized_fee >= 0),
    potential_saving NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (potential_saving >= 0),
    calculated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ml_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    qualification_probability NUMERIC(6,5) CHECK (qualification_probability BETWEEN 0 AND 1),
    risk_score NUMERIC(6,5) CHECK (risk_score BETWEEN 0 AND 1),
    risk_level VARCHAR(10) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    important_features JSONB NOT NULL DEFAULT '{}'::jsonb,
    recommendation TEXT,
    predicted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE processing_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_type VARCHAR(30) NOT NULL CHECK (job_type IN ('CSV_IMPORT', 'BULK_EVALUATION')),
    file_name VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    total_records INTEGER NOT NULL DEFAULT 0 CHECK (total_records >= 0),
    processed_records INTEGER NOT NULL DEFAULT 0 CHECK (processed_records >= 0),
    failed_records INTEGER NOT NULL DEFAULT 0 CHECK (failed_records >= 0),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_merchant_id ON transactions(merchant_id);
CREATE INDEX idx_transactions_scheme_id ON transactions(scheme_id);
CREATE INDEX idx_transactions_channel ON transactions(channel);
CREATE INDEX idx_transactions_mcc ON merchants(mcc);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_qualification_status ON qualification_results(qualification_status);
CREATE INDEX idx_rules_lookup ON interchange_rules(scheme_id, region, active, priority);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
