-- Allow team leads to view routing suggestions on the assignment desk.
UPDATE role
SET permissions = CASE
        WHEN permissions::jsonb ? 'routing:read' THEN permissions
        ELSE (permissions::jsonb || '["routing:read"]'::jsonb)::text
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE role_code = 'TEAM_LEAD';
