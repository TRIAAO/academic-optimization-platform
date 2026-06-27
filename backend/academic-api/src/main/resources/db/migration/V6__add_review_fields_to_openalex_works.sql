ALTER TABLE openalex_works
    ADD COLUMN review_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    ADD COLUMN review_note TEXT,
    ADD COLUMN reviewed_at TIMESTAMP;

CREATE INDEX idx_openalex_works_review_status
    ON openalex_works (review_status);