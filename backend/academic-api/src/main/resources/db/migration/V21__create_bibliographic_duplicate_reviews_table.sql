CREATE TABLE bibliographic_duplicate_reviews (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL REFERENCES researchers(id) ON DELETE CASCADE,
    left_source VARCHAR(30) NOT NULL,
    left_work_id UUID NOT NULL,
    right_source VARCHAR(30) NOT NULL,
    right_work_id UUID NOT NULL,
    similarity_score INTEGER NOT NULL,
    title_similarity INTEGER NOT NULL,
    doi_exact_match BOOLEAN NOT NULL DEFAULT FALSE,
    publication_year_compatible BOOLEAN NOT NULL DEFAULT FALSE,
    rationale TEXT NOT NULL,
    review_status VARCHAR(30) NOT NULL,
    reviewer_note TEXT,
    reviewed_by VARCHAR(180),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_bibliographic_duplicate_score
        CHECK (similarity_score BETWEEN 0 AND 100),
    CONSTRAINT chk_bibliographic_title_similarity
        CHECK (title_similarity BETWEEN 0 AND 100),
    CONSTRAINT uq_bibliographic_duplicate_pair
        UNIQUE (researcher_id, left_source, left_work_id, right_source, right_work_id)
);

CREATE INDEX idx_bibliographic_duplicate_researcher
    ON bibliographic_duplicate_reviews(researcher_id);

CREATE INDEX idx_bibliographic_duplicate_status
    ON bibliographic_duplicate_reviews(researcher_id, review_status);
