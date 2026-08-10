-- Workflow teams use authenticated users. Keep the legacy person-based team_member table for CMDB compatibility.
ALTER TABLE team ADD COLUMN IF NOT EXISTS leader_user_id BIGINT REFERENCES "user"(id);

CREATE TABLE IF NOT EXISTS team_user_member (
    team_id BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_team_user_member_user ON team_user_member(user_id);
CREATE INDEX IF NOT EXISTS idx_team_leader_user ON team(leader_user_id);

INSERT INTO team (name, org_id, team_code, team_type, status, created_at, updated_at)
SELECT 'Application Operations', o.id, 'APP_OPERATIONS_EN', 'SUPPORT', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization o WHERE o.code = 'IT_CENTER_EN'
ON CONFLICT (team_code) DO UPDATE SET name = EXCLUDED.name, org_id = EXCLUDED.org_id, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP;

INSERT INTO team (name, org_id, team_code, team_type, status, created_at, updated_at)
SELECT 'Quality Assurance', o.id, 'QUALITY_ASSURANCE_EN', 'TEST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization o WHERE o.code = 'IT_CENTER_EN'
ON CONFLICT (team_code) DO UPDATE SET name = EXCLUDED.name, org_id = EXCLUDED.org_id, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP;

UPDATE team
SET leader_user_id = (SELECT id FROM "user" WHERE username = 'lead01')
WHERE team_code = 'APP_OPERATIONS_EN';

INSERT INTO team_user_member (team_id, user_id)
SELECT t.id, u.id FROM team t CROSS JOIN "user" u
WHERE t.team_code = 'APP_OPERATIONS_EN' AND u.username IN ('technician01', 'lead01')
ON CONFLICT DO NOTHING;

INSERT INTO team_user_member (team_id, user_id)
SELECT t.id, u.id FROM team t CROSS JOIN "user" u
WHERE t.team_code = 'QUALITY_ASSURANCE_EN' AND u.username = 'tester01'
ON CONFLICT DO NOTHING;
