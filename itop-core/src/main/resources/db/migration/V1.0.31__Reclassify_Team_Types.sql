-- Reclassify team types: IT_TEAM (can receive requests) and USER_TEAM
UPDATE team SET team_type = 'IT_TEAM' WHERE team_type IN ('SUPPORT', 'TEST', 'OPERATIONS', 'HELPDESK', 'CHANGE', 'PROBLEM') OR team_type IS NULL;
