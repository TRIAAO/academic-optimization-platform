CREATE TABLE researchers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(180) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    phone VARCHAR(50),
    institution VARCHAR(180),
    department VARCHAR(180),
    academic_title VARCHAR(120),
    orcid_id VARCHAR(50),
    country VARCHAR(100) NOT NULL DEFAULT 'Angola',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);