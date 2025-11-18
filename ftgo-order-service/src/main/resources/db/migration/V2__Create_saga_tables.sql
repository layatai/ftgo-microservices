CREATE TABLE saga_instances (
    id VARCHAR(255) PRIMARY KEY,
    saga_type VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    saga_data TEXT,
    idempotency_key VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    failure_reason VARCHAR(500)
);

CREATE TABLE saga_step_executions (
    id VARCHAR(255) PRIMARY KEY,
    saga_instance_id VARCHAR(255) NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    result TEXT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    FOREIGN KEY (saga_instance_id) REFERENCES saga_instances(id) ON DELETE CASCADE
);

CREATE INDEX idx_saga_type ON saga_instances(saga_type);
CREATE INDEX idx_saga_state ON saga_instances(state);
CREATE INDEX idx_saga_idempotency ON saga_instances(idempotency_key);
CREATE INDEX idx_step_execution_saga ON saga_step_executions(saga_instance_id);
CREATE INDEX idx_step_execution_state ON saga_step_executions(state);

