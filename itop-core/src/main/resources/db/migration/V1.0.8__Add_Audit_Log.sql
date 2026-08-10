-- V1.0.8: 修复 audit_log 表结构以匹配 AuditLog 实体
-- audit_log 表在 V1.0.0 中已创建，但列结构与 AuditLog 实体不一致
-- 此迁移将旧列结构转换为新结构（V1.0.0 的旧列: old_values/new_values/changed_by/changed_at）

-- 添加实体需要的新列
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS field_name VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS old_value TEXT;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS new_value TEXT;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS username VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS description VARCHAR(500);

-- 添加 created_at 列（V1.0.0 使用 changed_at）
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 迁移旧数据到新列
UPDATE audit_log SET username = changed_by WHERE username IS NULL AND changed_by IS NOT NULL;
UPDATE audit_log SET created_at = changed_at WHERE created_at IS NULL AND changed_at IS NOT NULL;

-- 调整列类型以匹配实体定义
ALTER TABLE audit_log ALTER COLUMN entity_type TYPE VARCHAR(50);
ALTER TABLE audit_log ALTER COLUMN action TYPE VARCHAR(20);

-- 删除不再需要的旧列
ALTER TABLE audit_log DROP COLUMN IF EXISTS old_values;
ALTER TABLE audit_log DROP COLUMN IF EXISTS new_values;
ALTER TABLE audit_log DROP COLUMN IF EXISTS changed_by;
ALTER TABLE audit_log DROP COLUMN IF EXISTS changed_at;

-- 添加新索引
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);

COMMENT ON TABLE audit_log IS 'Audit log table for tracking entity changes';
COMMENT ON COLUMN audit_log.entity_type IS 'Type of entity (Ticket, User, Server, etc.)';
COMMENT ON COLUMN audit_log.entity_id IS 'ID of the entity';
COMMENT ON COLUMN audit_log.action IS 'Action type (CREATE, UPDATE, DELETE, STATUS_CHANGE)';
COMMENT ON COLUMN audit_log.field_name IS 'Name of the field that was changed';
COMMENT ON COLUMN audit_log.old_value IS 'Value before change';
COMMENT ON COLUMN audit_log.new_value IS 'Value after change';
COMMENT ON COLUMN audit_log.user_id IS 'ID of user who made the change';
COMMENT ON COLUMN audit_log.username IS 'Username of user who made the change';
COMMENT ON COLUMN audit_log.ip_address IS 'IP address of the request';
COMMENT ON COLUMN audit_log.user_agent IS 'User agent of the request';
COMMENT ON COLUMN audit_log.description IS 'Human-readable description of the change';

-- 为 team 和 attachment 表添加缺失的 updated_at 触发器
CREATE TRIGGER update_team_updated_at BEFORE UPDATE ON team
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attachment_updated_at BEFORE UPDATE ON attachment
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
