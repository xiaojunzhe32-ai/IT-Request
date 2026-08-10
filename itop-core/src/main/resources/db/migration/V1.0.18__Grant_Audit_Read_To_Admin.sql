-- ============================================================
-- V1.0.18: Grant audit:read permission to ADMIN role
-- ============================================================

-- Update ADMIN role to include all permissions explicitly
-- This ensures audit:read and other permissions work correctly

UPDATE role
SET permissions = '["*","audit:read","audit:write","user:read","user:write","org:read","org:write","team:read","team:write","role:read","role:write","ci:*","request:*","ticket:*","sla:*","service:*"]'
WHERE role_code = 'ADMIN';

-- Ensure admin user (id=1) has ADMIN role assigned
INSERT INTO user_role (user_id, role_id)
SELECT 1, id FROM role WHERE role_code = 'ADMIN'
ON CONFLICT DO NOTHING;