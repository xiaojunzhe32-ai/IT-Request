-- ============================================================
-- V1.0.17: IT Request Workflow - Roles & Permissions
-- Replace old CMDB roles with 5 workflow roles:
--   Requester, Technician, Tester, Team Lead, Admin
-- ============================================================

-- 1. Clean up old demo users (id 10-18) and their role assignments
DELETE FROM user_role WHERE user_id IN (10, 11, 12, 13, 14, 15, 16, 17, 18);
DELETE FROM "user" WHERE id IN (10, 11, 12, 13, 14, 15, 16, 17, 18);

-- 2. Remove old non-admin roles
DELETE FROM user_role WHERE role_id != 1;
DELETE FROM role WHERE id != 1;

-- 3. Update ADMIN role (id=1)
UPDATE role SET
    name = 'Administrator',
    role_code = 'ADMIN',
    permissions = '["*"]',
    is_system = true,
    description = 'System owner and super team lead. Full access to all modules.',
    status = 'active'
WHERE id = 1;

-- 4. Insert new workflow roles (let DB assign ids)
INSERT INTO role (name, role_code, status, is_system, permissions, description)
VALUES
('Requester', 'REQUESTER', 'active', true,
 '["request:create","request:read","request:comment"]',
 'Ordinary business user. Can create requests, view own/others requests, comment, confirm closure or report user test failure.'),
('Technician', 'TECHNICIAN', 'active', true,
 '["request:read","request:write","request:assign","request:transfer","request:transition","request:comment","team:read"]',
 'IT handling person. Handles assigned/team requests, adds work notes, transfers, submits to testing.'),
('Tester', 'TESTER', 'active', true,
 '["request:read","request:write","request:transition","request:test","request:comment","team:read"]',
 'IT internal tester. Verifies technician fixes, passes to resolved or returns to in progress.'),
('Team Lead', 'TEAM_LEAD', 'active', true,
 '["request:read","request:write","request:assign","request:transfer","request:transition","request:comment","team:read","team:write","user:read"]',
 'IT technical owner. Owns team queue, assigns/reassigns work, corrects adjacent status steps.')
ON CONFLICT (role_code) DO UPDATE SET
    name = EXCLUDED.name,
    permissions = EXCLUDED.permissions,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    is_system = EXCLUDED.is_system;

-- 5. Ensure admin user has ADMIN role
INSERT INTO user_role (user_id, role_id)
SELECT 1, id FROM role WHERE role_code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- 6. Create demo users for each role (password: admin123)
INSERT INTO "user" (name, username, password, email, first_name, last_name, phone, org_id, status, auth_method, language)
VALUES
('Alice Chen', 'requester01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'alice.chen@example.com', 'Alice', 'Chen', '13800138010', 1, 'active', 'LOCAL', 'en_US'),
('Bob Wang', 'technician01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'bob.wang@example.com', 'Bob', 'Wang', '13800138011', 1, 'active', 'LOCAL', 'en_US'),
('Carol Liu', 'tester01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'carol.liu@example.com', 'Carol', 'Liu', '13800138012', 1, 'active', 'LOCAL', 'en_US'),
('David Zhang', 'lead01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'david.zhang@example.com', 'David', 'Zhang', '13800138013', 1, 'active', 'LOCAL', 'en_US')
ON CONFLICT (username) DO NOTHING;

-- 7. Assign roles to demo users (lookup by role_code, not hardcoded id)
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'requester01' AND r.role_code = 'REQUESTER'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'technician01' AND r.role_code = 'TECHNICIAN'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'tester01' AND r.role_code = 'TESTER'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'lead01' AND r.role_code = 'TEAM_LEAD'
ON CONFLICT DO NOTHING;

-- Reset role sequence to avoid future id collisions
SELECT setval('role_id_seq', COALESCE((SELECT MAX(id) FROM role), 1), true);
