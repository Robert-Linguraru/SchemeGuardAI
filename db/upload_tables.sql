-- Run this after db/create_schema.sql.

CREATE TABLE IF NOT EXISTS upload_sessions (
    id UUID PRIMARY KEY,
    processing_job_id UUID REFERENCES processing_jobs(id) ON DELETE SET NULL,
    original_file_name VARCHAR(255) NOT NULL,
    expected_file_size_bytes BIGINT NOT NULL CHECK (expected_file_size_bytes > 0),
    expected_total_parts INTEGER NOT NULL CHECK (expected_total_parts > 0),
    expected_file_sha256 VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    uploaded_bytes BIGINT NOT NULL DEFAULT 0 CHECK (uploaded_bytes >= 0),
    uploaded_parts INTEGER NOT NULL DEFAULT 0 CHECK (uploaded_parts >= 0),
    processed_rows BIGINT NOT NULL DEFAULT 0 CHECK (processed_rows >= 0),
    error_message VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS upload_parts (
    id BIGSERIAL PRIMARY KEY,
    upload_id UUID NOT NULL REFERENCES upload_sessions(id) ON DELETE CASCADE,
    part_number INTEGER NOT NULL CHECK (part_number >= 0),
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    sha256 VARCHAR(64) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    CONSTRAINT uq_upload_parts_session_number
        UNIQUE (upload_id, part_number)
);

ALTER TABLE upload_sessions
    ADD COLUMN IF NOT EXISTS processing_job_id UUID
        REFERENCES processing_jobs(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_upload_parts_upload_id
    ON upload_parts(upload_id);

CREATE INDEX IF NOT EXISTS idx_upload_sessions_status
    ON upload_sessions(status);
