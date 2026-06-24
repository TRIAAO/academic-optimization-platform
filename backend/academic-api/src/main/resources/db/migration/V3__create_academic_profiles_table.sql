CREATE TABLE academic_profiles (
    id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL UNIQUE,
    research_area VARCHAR(180),
    biography TEXT,
    keywords TEXT,
    google_scholar_url VARCHAR(255),
    orcid_url VARCHAR(255),
    scopus_author_id VARCHAR(100),
    web_of_science_id VARCHAR(100),
    lattes_url VARCHAR(255),
    institutional_profile_url VARCHAR(255),
    profile_completion_percentage INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_academic_profiles_researcher
        FOREIGN KEY (researcher_id)
        REFERENCES researchers (id)
);