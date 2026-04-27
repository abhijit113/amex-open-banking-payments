CREATE TABLE IF NOT EXISTS payout (
    id VARCHAR(64) PRIMARY KEY,
    trace_id VARCHAR(64) NOT NULL,
    end_to_end_id VARCHAR(64) NOT NULL,
    amount_in_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(64) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    external_provider_payout_id VARCHAR(128),
    beneficiary_account_holder_name VARCHAR(140) NOT NULL,
    beneficiary_sort_code VARCHAR(16) NOT NULL,
    beneficiary_account_number_last4 VARCHAR(4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payout_end_to_end_id
    ON payout(end_to_end_id);

CREATE INDEX IF NOT EXISTS idx_payout_external_provider_payout_id
    ON payout(external_provider_payout_id);