CREATE TABLE crossref_validations (
    id UUID PRIMARY KEY,
    openalex_work_id UUID NOT NULL,
    researcher_id UUID NOT NULL,

    doi_submitted VARCHAR(255),
    doi_found VARCHAR(255),

    title_submitted TEXT NOT NULL,
    title_found TEXT,

    publisher TEXT,
    container_title TEXT,
    publication_type VARCHAR(100),
    publication_year INTEGER,

    is_doi_valid BOOLEAN NOT NULL DEFAULT FALSE,
    title_similarity NUMERIC(5,2) NOT NULL DEFAULT 0,
    match_status VARCHAR(50) NOT NULL,
    message TEXT,

    raw_source VARCHAR(50) NOT NULL DEFAULT 'CROSSREF',

    validated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_crossref_validations_openalex_work
        FOREIGN KEY (openalex_work_id)
        REFERENCES openalex_works (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_crossref_validations_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
);

CREATE INDEX idx_crossref_validations_openalex_work_id
    ON crossref_validations (openalex_work_id);

CREATE INDEX idx_crossref_validations_researcher_id
    ON crossref_validations (researcher_id);

CREATE INDEX idx_crossref_validations_match_status
    ON crossref_validations (match_status);

CREATE INDEX idx_crossref_validations_doi_found
    ON crossref_validations (doi_found);