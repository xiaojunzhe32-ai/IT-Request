-- Multi-leader support: leaders are a subset of team members
CREATE TABLE IF NOT EXISTS team_user_leader (
    team_id BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

-- Migrate existing single leaderUserId into the new table
INSERT INTO team_user_leader (team_id, user_id)
SELECT t.id, t.leader_user_id
FROM team t
WHERE t.leader_user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM team_user_leader tul WHERE tul.team_id = t.id AND tul.user_id = t.leader_user_id
  );
