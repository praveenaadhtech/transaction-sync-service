-- Create merchants table
-- Stores merchant information synced from Privvy API

CREATE TABLE merchants (
    id BIGSERIAL PRIMARY KEY,
    mid VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    last_synced_at TIMESTAMPTZ
);

-- Create indexes for better query performance
CREATE INDEX idx_status ON merchants(status);
CREATE INDEX idx_mid ON merchants(mid);

