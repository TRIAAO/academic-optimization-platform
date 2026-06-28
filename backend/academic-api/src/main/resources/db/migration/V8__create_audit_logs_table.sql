CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,

    actor_email VARCHAR(255),
    actor_role VARCHAR(100),

    action VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,

    http_method VARCHAR(20) NOT NULL,
    endpoint TEXT NOT NULL,

    target_type VARCHAR(100),
    target_id VARCHAR(255),

    status VARCHAR(50) NOT NULL,
    http_status INTEGER NOT NULL,

    ip_address VARCHAR(100),
    user_agent TEXT,

    message TEXT,

    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_actor_email
    ON audit_logs (actor_email);

CREATE INDEX idx_audit_logs_action
    ON audit_logs (action);

CREATE INDEX idx_audit_logs_module
    ON audit_logs (module);

CREATE INDEX idx_audit_logs_status
    ON audit_logs (status);

CREATE INDEX idx_audit_logs_http_status
    ON audit_logs (http_status);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs (created_at);