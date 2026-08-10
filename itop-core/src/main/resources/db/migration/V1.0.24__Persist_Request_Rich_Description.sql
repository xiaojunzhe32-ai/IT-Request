-- Persist the sanitized rich-text representation separately from the searchable plain-text description.
ALTER TABLE user_request
    ADD COLUMN IF NOT EXISTS description_html TEXT;

COMMENT ON COLUMN user_request.description_html IS
    'Sanitized rich-text request description. Embedded screenshots are stored as attachments and referenced by attachment ID.';
