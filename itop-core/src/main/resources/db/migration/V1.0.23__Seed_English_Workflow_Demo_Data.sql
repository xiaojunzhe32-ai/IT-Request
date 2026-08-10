-- ============================================================
-- V1.0.23: English workflow demo organizations and real-login alignment
-- Keeps the current product UI English while preserving existing legacy data.
-- ============================================================

-- English organization tree used by the request workflow demo.
INSERT INTO organization (name, code, type, status, description, created_at, updated_at)
VALUES ('Business Organization', 'BUSINESS_ORG', 'COMPANY', 'ACTIVE', 'English demo root for request workflow.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization (name, code, type, parent_id, status, description, created_at, updated_at)
SELECT 'Finance Department', 'FINANCE_DEPT', 'DEPARTMENT', root.id, 'ACTIVE', 'Finance requester demo organization.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization root WHERE root.code = 'BUSINESS_ORG'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization (name, code, type, parent_id, status, description, created_at, updated_at)
SELECT 'HR Department', 'HR_DEPT', 'DEPARTMENT', root.id, 'ACTIVE', 'Human resources demo organization.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization root WHERE root.code = 'BUSINESS_ORG'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization (name, code, type, parent_id, status, description, created_at, updated_at)
SELECT 'Branch Office', 'BRANCH_OFFICE', 'DEPARTMENT', root.id, 'ACTIVE', 'Branch office demo organization.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization root WHERE root.code = 'BUSINESS_ORG'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization (name, code, type, parent_id, status, description, created_at, updated_at)
SELECT 'Administration Office', 'ADMIN_OFFICE', 'DEPARTMENT', root.id, 'ACTIVE', 'Administration demo organization.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization root WHERE root.code = 'BUSINESS_ORG'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization (name, code, type, parent_id, status, description, created_at, updated_at)
SELECT 'IT Center', 'IT_CENTER_EN', 'IT_DEPT', root.id, 'ACTIVE', 'English demo IT organization.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organization root WHERE root.code = 'BUSINESS_ORG'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

-- Move seeded workflow users to English demo organizations.
UPDATE "user"
SET org_id = (SELECT id FROM organization WHERE code = 'FINANCE_DEPT'),
    updated_at = CURRENT_TIMESTAMP
WHERE username = 'requester01';

UPDATE "user"
SET org_id = (SELECT id FROM organization WHERE code = 'IT_CENTER_EN'),
    updated_at = CURRENT_TIMESTAMP
WHERE username IN ('technician01', 'tester01', 'lead01');

-- Demo access scope: requester can select English business demo orgs;
-- IT roles can work across the English workflow demo tree.
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, o.id, true
FROM "user" u
JOIN organization o ON o.code = 'BUSINESS_ORG'
WHERE u.username IN ('requester01', 'technician01', 'tester01', 'lead01')
ON CONFLICT (user_id, org_id) DO UPDATE SET include_children = EXCLUDED.include_children;

-- Let workflow users load organization options through the existing organization API.
UPDATE role
SET permissions = '["request:create","request:read","request:comment","org:read"]',
    updated_at = CURRENT_TIMESTAMP
WHERE role_code = 'REQUESTER';

UPDATE role
SET permissions = '["request:read","request:write","request:assign","request:transfer","request:transition","request:comment","team:read","user:read","org:read"]',
    updated_at = CURRENT_TIMESTAMP
WHERE role_code = 'TECHNICIAN';

UPDATE role
SET permissions = '["request:read","request:write","request:transition","request:test","request:comment","team:read","user:read","org:read"]',
    updated_at = CURRENT_TIMESTAMP
WHERE role_code = 'TESTER';

UPDATE role
SET permissions = '["request:read","request:write","request:assign","request:transfer","request:transition","request:comment","team:read","team:write","user:read","org:read"]',
    updated_at = CURRENT_TIMESTAMP
WHERE role_code = 'TEAM_LEAD';
