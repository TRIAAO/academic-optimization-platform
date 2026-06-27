CREATE TABLE openalex_works (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    openalex_id VARCHAR(255) NOT NULL,
    doi VARCHAR(255),
    title TEXT NOT NULL,
    work_type VARCHAR(100),
    publication_year INTEGER,
    publication_date VARCHAR(30),
    source_name TEXT,
    cited_by_count INTEGER NOT NULL DEFAULT 0,
    is_open_access BOOLEAN,
    open_access_status VARCHAR(100),
    openalex_url TEXT,
    doi_url TEXT,
    raw_source VARCHAR(50) NOT NULL DEFAULT 'OPENALEX',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_openalex_works_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
);

CREATE UNIQUE INDEX uk_openalex_works_researcher_openalex_id
    ON openalex_works (researcher_id, openalex_id);

CREATE INDEX idx_openalex_works_researcher_id
    ON openalex_works (researcher_id);

CREATE INDEX idx_openalex_works_doi
    ON openalex_works (doi);