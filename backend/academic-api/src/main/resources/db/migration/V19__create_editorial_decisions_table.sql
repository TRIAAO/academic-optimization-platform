CREATE TABLE editorial_decisions (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    openalex_work_id UUID NOT NULL,
    journal_name VARCHAR(500) NOT NULL,
    publisher VARCHAR(500),
    issns TEXT,
    relevance_score INTEGER NOT NULL,
    official_url TEXT,
    status VARCHAR(50) NOT NULL,
    scope_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    peer_review_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    indexing_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    fees_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    language_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    deadlines_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    reviewed_by VARCHAR(180) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_editorial_decisions_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_editorial_decisions_openalex_work
        FOREIGN KEY (openalex_work_id)
        REFERENCES openalex_works (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_editorial_decisions_relevance_score
        CHECK (relevance_score BETWEEN 0 AND 100),

    CONSTRAINT uk_editorial_decisions_researcher_work
        UNIQUE (researcher_id, openalex_work_id)
);

CREATE INDEX idx_editorial_decisions_researcher_id
    ON editorial_decisions (researcher_id);

CREATE INDEX idx_editorial_decisions_work_id
    ON editorial_decisions (openalex_work_id);

CREATE INDEX idx_editorial_decisions_status
    ON editorial_decisions (status);
