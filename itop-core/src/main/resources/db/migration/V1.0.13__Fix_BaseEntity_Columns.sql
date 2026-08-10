-- Add missing BaseEntity columns (name, version, created_by, updated_by) to attachment table
ALTER TABLE attachment ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE attachment ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
ALTER TABLE attachment ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE attachment ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Set name from original_name for any existing rows, then enforce NOT NULL
UPDATE attachment SET name = original_name WHERE name IS NULL;
ALTER TABLE attachment ALTER COLUMN name SET NOT NULL;

-- Add missing version column to team table
ALTER TABLE team ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
UPDATE team SET version = 0 WHERE version IS NULL;

COMMENT ON COLUMN attachment.name IS 'Display name (inherited from BaseEntity)';
COMMENT ON COLUMN attachment.version IS 'Optimistic lock version (inherited from BaseEntity)';
COMMENT ON COLUMN attachment.created_by IS 'Creator username (inherited from BaseEntity)';
COMMENT ON COLUMN attachment.updated_by IS 'Last updater username (inherited from BaseEntity)';
COMMENT ON COLUMN team.version IS 'Optimistic lock version (inherited from BaseEntity)';
