CREATE TABLE scientometric_metrics (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    source VARCHAR(80) NOT NULL DEFAULT 'MANUAL_GOOGLE_SCHOLAR',
    google_scholar_author_id VARCHAR(120),
    google_scholar_profile_url VARCHAR(500),
    h_index_total INTEGER,
    h_index_last_six_years INTEGER,
    i10_index_total INTEGER,
    i10_index_last_six_years INTEGER,
    citations_total INTEGER,
    citations_last_six_years INTEGER,
    d_index INTEGER,
    verified_email VARCHAR(180),
    institutional_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    interests TEXT,
    notes TEXT,
    snapshot_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_scientometric_metrics_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id),

    CONSTRAINT uk_scientometric_metrics_researcher_source_snapshot
        UNIQUE (researcher_id, source, snapshot_date),

    CONSTRAINT chk_scientometric_metrics_h_index_total
        CHECK (h_index_total IS NULL OR h_index_total >= 0),

    CONSTRAINT chk_scientometric_metrics_h_index_last_six_years
        CHECK (h_index_last_six_years IS NULL OR h_index_last_six_years >= 0),

    CONSTRAINT chk_scientometric_metrics_i10_index_total
        CHECK (i10_index_total IS NULL OR i10_index_total >= 0),

    CONSTRAINT chk_scientometric_metrics_i10_index_last_six_years
        CHECK (i10_index_last_six_years IS NULL OR i10_index_last_six_years >= 0),

    CONSTRAINT chk_scientometric_metrics_citations_total
        CHECK (citations_total IS NULL OR citations_total >= 0),

    CONSTRAINT chk_scientometric_metrics_citations_last_six_years
        CHECK (citations_last_six_years IS NULL OR citations_last_six_years >= 0),

    CONSTRAINT chk_scientometric_metrics_d_index
        CHECK (d_index IS NULL OR d_index >= 0)
);

CREATE INDEX idx_scientometric_metrics_researcher
    ON scientometric_metrics (researcher_id);

CREATE INDEX idx_scientometric_metrics_snapshot_date
    ON scientometric_metrics (snapshot_date DESC);

CREATE INDEX idx_scientometric_metrics_source
    ON scientometric_metrics (source);