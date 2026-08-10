-- Attachment table
CREATE TABLE attachment (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    description VARCHAR(500),
    uploader_id BIGINT,
    download_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attachment_entity ON attachment(entity_type, entity_id);
CREATE INDEX idx_attachment_uploader ON attachment(uploader_id);

COMMENT ON TABLE attachment IS 'Attachment table for storing file metadata';
COMMENT ON COLUMN attachment.entity_type IS 'Type of entity (TICKET, CHANGE, PROBLEM, etc.)';
COMMENT ON COLUMN attachment.entity_id IS 'ID of the related entity';
COMMENT ON COLUMN attachment.file_name IS 'Unique file name on storage';
COMMENT ON COLUMN attachment.original_name IS 'Original file name uploaded by user';
COMMENT ON COLUMN attachment.file_path IS 'File path in storage';
COMMENT ON COLUMN attachment.file_size IS 'File size in bytes';
COMMENT ON COLUMN attachment.content_type IS 'MIME type';
COMMENT ON COLUMN attachment.uploader_id IS 'User who uploaded the file';