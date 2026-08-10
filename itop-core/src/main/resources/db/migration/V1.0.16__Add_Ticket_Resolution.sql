-- Add resolution column to ticket table (maps Ticket.resolution enum field)
-- Hibernate update mode already added this column; using IF NOT EXISTS for migration safety
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS resolution VARCHAR(50) DEFAULT 'NONE';
COMMENT ON COLUMN ticket.resolution IS 'Resolution status enum (NONE, RESOLVED, CLOSED, REOPENED, etc.)';
