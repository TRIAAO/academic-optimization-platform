CREATE TABLE orcid_import_logs (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    orcid_id VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    total_found INTEGER NOT NULL DEFAULT 0,
    total_imported INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,

    CONSTRAINT fk_orcid_import_logs_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
);

CREATE TABLE orcid_works (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    import_log_id UUID,
    orcid_id VARCHAR(50) NOT NULL,
    put_code VARCHAR(100),
    title TEXT NOT NULL,
    work_type VARCHAR(100),
    publication_year INTEGER,
    publication_month INTEGER,
    publication_day INTEGER,
    journal_title TEXT,
    doi VARCHAR(255),
    external_url TEXT,
    source_name VARCHAR(255),
    raw_source VARCHAR(50) NOT NULL DEFAULT 'ORCID',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_orcid_works_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id),

    CONSTRAINT fk_orcid_works_import_log
        FOREIGN KEY (import_log_id)
        REFERENCES orcid_import_logs (id)
);

CREATE UNIQUE INDEX uk_orcid_works_researcher_put_code
    ON orcid_works (researcher_id, put_code)
    WHERE put_code IS NOT NULL;

CREATE INDEX idx_orcid_works_researcher_id
    ON orcid_works (researcher_id);

CREATE INDEX idx_orcid_works_doi
    ON orcid_works (doi);