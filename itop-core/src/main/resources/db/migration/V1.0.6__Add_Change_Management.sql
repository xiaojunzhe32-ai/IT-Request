-- V1.0.6__Add_Change_Management.sql
-- Change Management schema

-- Change Request (变更请求)
CREATE TABLE change_request (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Ticket fields (from ticket table via JOINED inheritance)
    org_id BIGINT REFERENCES organization(id),
    caller_id BIGINT REFERENCES contact(id),
    agent_id BIGINT REFERENCES person(id),
    team_id BIGINT REFERENCES team(id),
    service_id BIGINT REFERENCES service(id),
    request_type_id BIGINT,
    title VARCHAR(255) NOT NULL,
    impact VARCHAR(20) DEFAULT '2',
    urgency VARCHAR(20) DEFAULT '2',
    priority VARCHAR(20) DEFAULT '2',
    ticket_status VARCHAR(50) DEFAULT 'NEW',
    resolution VARCHAR(50) DEFAULT 'NONE',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    last_update_date TIMESTAMP,
    close_date TIMESTAMP,
    solution TEXT,
    final_class VARCHAR(100) NOT NULL DEFAULT 'ChangeRequest',
    ticket_type VARCHAR(50) DEFAULT 'change',

    -- Change specific fields
    change_number VARCHAR(50) UNIQUE NOT NULL,
    change_type VARCHAR(50) DEFAULT 'NORMAL',  -- NORMAL, STANDARD, EMERGENCY
    change_category VARCHAR(50) DEFAULT 'OTHER',  -- APPLICATION, INFRASTRUCTURE, DOCUMENTATION, OTHER
    change_reason TEXT,
    risk_assessment TEXT,
    rollback_plan TEXT,
    implementation_plan TEXT,
    test_plan TEXT,
    change_owner_id BIGINT REFERENCES person(id),
    approver_id BIGINT REFERENCES person(id),
    approval_date TIMESTAMP,
    planned_start_date TIMESTAMP,
    planned_end_date TIMESTAMP,
    actual_start_date TIMESTAMP,
    actual_end_date TIMESTAMP,
    parent_change_id BIGINT REFERENCES change_request(id),
    related_problem_id BIGINT
);

CREATE INDEX idx_change_number ON change_request(change_number);
CREATE INDEX idx_change_status ON change_request(ticket_status);
CREATE INDEX idx_change_type ON change_request(change_type);
CREATE INDEX idx_change_org ON change_request(org_id);
CREATE INDEX idx_change_team ON change_request(team_id);
CREATE INDEX idx_change_owner ON change_request(change_owner_id);

-- Change Task (变更任务)
CREATE TABLE change_task (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    change_id BIGINT NOT NULL REFERENCES change_request(id),
    task_type VARCHAR(50) DEFAULT 'IMPLEMENTATION',  -- PLANNING, REVIEW, IMPLEMENTATION, TESTING, ROLLBACK
    task_status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
    assignee_id BIGINT REFERENCES person(id),
    planned_start_date TIMESTAMP,
    planned_end_date TIMESTAMP,
    actual_start_date TIMESTAMP,
    actual_end_date TIMESTAMP,
    instructions TEXT,
    result TEXT,
    sort_order INTEGER DEFAULT 0
);

CREATE INDEX idx_change_task_change ON change_task(change_id);
CREATE INDEX idx_change_task_status ON change_task(task_status);
CREATE INDEX idx_change_task_assignee ON change_task(assignee_id);

-- Change CI (影响配置项)
CREATE TABLE change_ci (
    change_id BIGINT NOT NULL REFERENCES change_request(id),
    ci_id BIGINT NOT NULL REFERENCES configuration_item(id),
    impact_type VARCHAR(50) DEFAULT 'AFFECTED',  -- AFFECTED, UPDATED, ADDED, REMOVED
    PRIMARY KEY (change_id, ci_id)
);

-- Insert default change types
COMMENT ON TABLE change_request IS 'Change Request - Formal proposal for changes to IT services or infrastructure';
COMMENT ON TABLE change_task IS 'Change Task - Individual tasks within a change request';
COMMENT ON COLUMN change_request.change_type IS 'NORMAL: Standard change requiring approval, STANDARD: Pre-approved standard change, EMERGENCY: Emergency change';

-- Triggers for updated_at
CREATE TRIGGER update_change_request_updated_at BEFORE UPDATE ON change_request
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_change_task_updated_at BEFORE UPDATE ON change_task
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();