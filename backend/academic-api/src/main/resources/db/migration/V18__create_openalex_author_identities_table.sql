CREATE TABLE openalex_author_identities (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    openalex_author_id VARCHAR(32) NOT NULL,
    display_name VARCHAR(255),
    orcid_id VARCHAR(50),
    last_known_institution VARCHAR(255),
    last_known_country_code VARCHAR(10),
    works_count INTEGER NOT NULL DEFAULT 0,
    cited_by_count INTEGER NOT NULL DEFAULT 0,
    verification_source VARCHAR(30) NOT NULL,
    confirmed_at TIMESTAMP NOT NULL,
    last_synced_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_openalex_author_identities_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_openalex_author_identities_works_count
        CHECK (works_count >= 0),

    CONSTRAINT ck_openalex_author_identities_cited_by_count
        CHECK (cited_by_count >= 0),

    CONSTRAINT ck_openalex_author_identities_verification_source
        CHECK (verification_source IN ('ORCID', 'MANUAL'))
);

CREATE UNIQUE INDEX uk_openalex_author_identities_researcher
    ON openalex_author_identities (researcher_id);

CREATE UNIQUE INDEX uk_openalex_author_identities_author
    ON openalex_author_identities (openalex_author_id);

CREATE INDEX idx_openalex_author_identities_last_synced_at
    ON openalex_author_identities (last_synced_at);
