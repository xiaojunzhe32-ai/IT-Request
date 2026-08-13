-- V1.0.29: Drop organization requirement from teams
-- Organization is being abolished from the product surface.
-- Teams no longer require an org_id; the column is kept for backward compatibility.

ALTER TABLE team ALTER COLUMN org_id DROP NOT NULL;
