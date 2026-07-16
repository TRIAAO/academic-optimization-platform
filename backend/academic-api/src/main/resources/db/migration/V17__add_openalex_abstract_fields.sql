ALTER TABLE openalex_works
    ADD COLUMN abstract_text TEXT,
    ADD COLUMN abstract_language VARCHAR(20),
    ADD COLUMN abstract_pt TEXT,
    ADD COLUMN abstract_en TEXT,
    ADD COLUMN abstract_translations_updated_at TIMESTAMP;

CREATE INDEX idx_openalex_works_abstract_language
    ON openalex_works (abstract_language);
