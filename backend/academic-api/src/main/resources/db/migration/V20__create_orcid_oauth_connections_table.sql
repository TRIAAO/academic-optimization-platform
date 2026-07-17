CREATE TABLE orcid_oauth_connections (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    orcid_id VARCHAR(50) NOT NULL,
    authenticated_name VARCHAR(255),
    token_type VARCHAR(50),
    scope VARCHAR(255) NOT NULL,
    encrypted_access_token TEXT NOT NULL,
    encrypted_refresh_token TEXT,
    expires_at TIMESTAMP,
    connected_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,

    CONSTRAINT fk_orcid_oauth_connections_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_orcid_oauth_connections_researcher
        UNIQUE (researcher_id),

    CONSTRAINT uk_orcid_oauth_connections_orcid
        UNIQUE (orcid_id)
);

CREATE INDEX idx_orcid_oauth_connections_active
    ON orcid_oauth_connections (researcher_id, revoked_at);

CREATE INDEX idx_orcid_oauth_connections_orcid_id
    ON orcid_oauth_connections (orcid_id);
