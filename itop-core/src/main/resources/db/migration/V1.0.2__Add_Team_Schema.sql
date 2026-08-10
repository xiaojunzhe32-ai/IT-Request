-- Team table for support/helpdesk teams
CREATE TABLE IF NOT EXISTS team (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    org_id BIGINT NOT NULL REFERENCES organization(id),
    team_code VARCHAR(50) UNIQUE,
    team_type VARCHAR(50) DEFAULT 'SUPPORT',  -- HELPDESK, SUPPORT, CHANGE, PROBLEM
    leader_id BIGINT REFERENCES person(id),
    email VARCHAR(100),
    phone VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Team member relationship (many-to-many)
CREATE TABLE IF NOT EXISTS team_member (
    team_id BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, person_id)
);

-- Add team_id to ticket table
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS team_id BIGINT REFERENCES team(id);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_team_org ON team(org_id);
CREATE INDEX IF NOT EXISTS idx_team_type ON team(team_type);
CREATE INDEX IF NOT EXISTS idx_team_leader ON team(leader_id);
CREATE INDEX IF NOT EXISTS idx_ticket_team ON ticket(team_id);

-- Insert default teams for testing
INSERT INTO team (name, org_id, team_code, team_type, status)
SELECT '服务台一线', id, 'HELPDESK_L1', 'HELPDESK', 'ACTIVE'
FROM organization WHERE code = 'GOV_ORG' AND NOT EXISTS (SELECT 1 FROM team WHERE team_code = 'HELPDESK_L1');

INSERT INTO team (name, org_id, team_code, team_type, status)
SELECT '应用运维组', id, 'APP_SUPPORT', 'SUPPORT', 'ACTIVE'
FROM organization WHERE code = 'GOV_ORG' AND NOT EXISTS (SELECT 1 FROM team WHERE team_code = 'APP_SUPPORT');

INSERT INTO team (name, org_id, team_code, team_type, status)
SELECT '网络运维组', id, 'NETWORK_SUPPORT', 'SUPPORT', 'ACTIVE'
FROM organization WHERE code = 'GOV_ORG' AND NOT EXISTS (SELECT 1 FROM team WHERE team_code = 'NETWORK_SUPPORT');

INSERT INTO team (name, org_id, team_code, team_type, status)
SELECT '基础设施组', id, 'INFRA_SUPPORT', 'SUPPORT', 'ACTIVE'
FROM organization WHERE code = 'GOV_ORG' AND NOT EXISTS (SELECT 1 FROM team WHERE team_code = 'INFRA_SUPPORT');