-- Remove TESTER role and recompute user roles based on team membership.

-- 1. Delete TESTER role associations and the role itself
DELETE FROM user_role WHERE role_id IN (SELECT id FROM role WHERE role_code = 'TESTER');
DELETE FROM role WHERE role_code = 'TESTER';

-- 2. Clear all non-ADMIN roles from users (will be re-assigned below)
DELETE FROM user_role WHERE role_id NOT IN (SELECT id FROM role WHERE role_code = 'ADMIN');

-- 3. Assign REQUESTER to all team members
INSERT INTO user_role (user_id, role_id)
SELECT DISTINCT tum.user_id, r.id
FROM team_user_member tum
CROSS JOIN (SELECT id FROM role WHERE role_code = 'REQUESTER') r
WHERE NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = tum.user_id AND ur.role_id = r.id
);

-- 4. Assign TECHNICIAN to IT_TEAM members
INSERT INTO user_role (user_id, role_id)
SELECT DISTINCT tum.user_id, r.id
FROM team_user_member tum
JOIN team t ON t.id = tum.team_id
CROSS JOIN (SELECT id FROM role WHERE role_code = 'TECHNICIAN') r
WHERE t.team_type = 'IT_TEAM'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = tum.user_id AND ur.role_id = r.id
);

-- 5. Assign TEAM_LEAD to all team leaders
INSERT INTO user_role (user_id, role_id)
SELECT DISTINCT tul.user_id, r.id
FROM team_user_leader tul
CROSS JOIN (SELECT id FROM role WHERE role_code = 'TEAM_LEAD') r
WHERE NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = tul.user_id AND ur.role_id = r.id
);
