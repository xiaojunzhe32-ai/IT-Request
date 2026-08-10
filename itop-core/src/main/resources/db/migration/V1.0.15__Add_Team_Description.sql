-- Add missing description column to team table (inherited from BaseEntity, mapped as TEXT)
ALTER TABLE team ADD COLUMN IF NOT EXISTS description TEXT;
