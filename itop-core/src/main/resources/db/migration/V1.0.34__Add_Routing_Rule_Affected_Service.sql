-- Add affected_service column to routing_rule table
-- Allows routing rules to match by the affected service / system selected by the user.

ALTER TABLE routing_rule ADD COLUMN IF NOT EXISTS affected_service VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_routing_rule_service ON routing_rule(affected_service);
