-- Ticket History for state machine tracking
CREATE TABLE IF NOT EXISTS ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    old_agent_id BIGINT,
    new_agent_id BIGINT,
    old_team_id BIGINT,
    new_team_id BIGINT,
    comment TEXT,
    user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_history_ticket_id ON ticket_history(ticket_id);
CREATE INDEX IF NOT EXISTS idx_ticket_history_created_at ON ticket_history(created_at);

COMMENT ON TABLE ticket_history IS 'Tracks all state transitions and changes to tickets';
COMMENT ON COLUMN ticket_history.action IS 'Action performed: created, assigned, reassigned, status_changed, resolved, closed, reopened';