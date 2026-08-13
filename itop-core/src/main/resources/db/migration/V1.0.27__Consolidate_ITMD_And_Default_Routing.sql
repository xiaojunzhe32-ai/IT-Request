-- Consolidate the demo IT teams into one shared ITMD team.
-- Keep routing rules as optional overrides; default route stays on ITMD.

DO $$
DECLARE
    itmd_team_id BIGINT;
    qa_team_id BIGINT;
BEGIN
    UPDATE team
    SET name = 'ITMD',
        team_code = 'ITMD',
        team_type = 'SUPPORT',
        status = 'ACTIVE',
        updated_at = CURRENT_TIMESTAMP
    WHERE team_code = 'APP_OPERATIONS_EN';

    SELECT id INTO itmd_team_id
    FROM team
    WHERE team_code = 'ITMD'
    LIMIT 1;

    IF itmd_team_id IS NOT NULL THEN
        UPDATE team
        SET leader_user_id = (SELECT id FROM "user" WHERE username = 'lead01' LIMIT 1),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = itmd_team_id;

        INSERT INTO team_user_member (team_id, user_id)
        SELECT itmd_team_id, u.id
        FROM "user" u
        WHERE u.username IN ('technician01', 'tester01', 'lead01')
        ON CONFLICT DO NOTHING;
    END IF;

    SELECT id INTO qa_team_id
    FROM team
    WHERE team_code = 'QUALITY_ASSURANCE_EN'
    LIMIT 1;

    IF qa_team_id IS NOT NULL AND itmd_team_id IS NOT NULL AND qa_team_id <> itmd_team_id THEN
        UPDATE ticket
        SET team_id = itmd_team_id,
            last_update_date = CURRENT_TIMESTAMP
        WHERE team_id = qa_team_id;

        DELETE FROM team_user_member WHERE team_id = qa_team_id;
        DELETE FROM team WHERE id = qa_team_id;
    END IF;
END $$;

UPDATE routing_rule
SET team_id = (SELECT id FROM team WHERE team_code = 'ITMD' LIMIT 1),
    enabled = true,
    description = 'Default route to ITMD. Optional rules stay disabled unless an admin enables them.',
    updated_at = CURRENT_TIMESTAMP
WHERE is_fallback = true;

UPDATE routing_rule
SET enabled = false,
    updated_at = CURRENT_TIMESTAMP
WHERE is_fallback = false;
