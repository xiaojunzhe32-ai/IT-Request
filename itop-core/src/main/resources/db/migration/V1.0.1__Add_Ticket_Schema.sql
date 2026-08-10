-- V1.0.1__Add_Ticket_Schema.sql
-- Add ticket management tables

-- Ticket base table
CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    org_id BIGINT REFERENCES organization(id),
    caller_id BIGINT REFERENCES contact(id),
    agent_id BIGINT REFERENCES person(id),
    team_id BIGINT,
    service_id BIGINT,
    request_type_id BIGINT,
    title VARCHAR(255) NOT NULL,
    impact VARCHAR(20) DEFAULT '2',
    urgency VARCHAR(20) DEFAULT '2',
    priority VARCHAR(20) DEFAULT '2',
    ticket_status VARCHAR(50) DEFAULT 'NEW',
    resolution_status VARCHAR(50) DEFAULT 'NONE',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    last_update_date TIMESTAMP,
    close_date TIMESTAMP,
    solution TEXT,
    final_class VARCHAR(100) NOT NULL,
    ticket_type VARCHAR(50) NOT NULL
);

CREATE INDEX idx_ticket_org ON ticket(org_id);
CREATE INDEX idx_ticket_status ON ticket(ticket_status);
CREATE INDEX idx_ticket_type ON ticket(ticket_type);
CREATE INDEX idx_ticket_final_class ON ticket(final_class);
CREATE INDEX idx_ticket_caller ON ticket(caller_id);
CREATE INDEX idx_ticket_agent ON ticket(agent_id);

-- UserRequest table
CREATE TABLE user_request (
    id BIGINT PRIMARY KEY REFERENCES ticket(id),
    origin VARCHAR(50) DEFAULT 'portal',
    approver_id BIGINT REFERENCES person(id),
    approval_date TIMESTAMP,
    expected_date TIMESTAMP,
    related_problem_id BIGINT,
    related_change_id BIGINT
);

CREATE INDEX idx_ureq_approver ON user_request(approver_id);

-- Incident table
CREATE TABLE incident (
    id BIGINT PRIMARY KEY REFERENCES ticket(id),
    incident_number VARCHAR(50) UNIQUE,
    incident_type VARCHAR(50) DEFAULT 'INCIDENT',
    affected_ci_id BIGINT REFERENCES configuration_item(id),
    workgroup_id BIGINT,
    known_error_id BIGINT,
    related_problem_id BIGINT,
    origin VARCHAR(50) DEFAULT 'MONITORING'
);

CREATE INDEX idx_incident_number ON incident(incident_number);
CREATE INDEX idx_incident_ci ON incident(affected_ci_id);

-- Add trigger for ticket table
CREATE TRIGGER update_ticket_updated_at BEFORE UPDATE ON ticket
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample tickets
INSERT INTO ticket (name, title, org_id, ticket_status, priority, impact, urgency, final_class, ticket_type)
VALUES
('Sample User Request', '需要新的开发环境', 1, 'NEW', '2', '2', '2', 'UserRequest', 'request'),
('Sample Incident', '生产服务器无法访问', 1, 'ASSIGNED', '1', '1', '1', 'Incident', 'incident');