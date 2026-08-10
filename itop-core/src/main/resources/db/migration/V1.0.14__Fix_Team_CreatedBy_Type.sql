-- Fix team.created_by and team.updated_by column types: bigint -> varchar(100)
-- BaseEntity defines createdBy/updatedBy as String (VARCHAR(100)), but team table used BIGINT
ALTER TABLE team ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR;
ALTER TABLE team ALTER COLUMN updated_by TYPE VARCHAR(100) USING updated_by::VARCHAR;
