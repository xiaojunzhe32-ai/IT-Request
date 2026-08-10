-- ============================================================
-- V1.0.20: 请求工作流状态模型对齐 + 请求字段补全
--   1. ticket_history schema 对齐实体（V1.0.9 与 V1.0.11 不一致）
--   2. ticket_status 迁移到新工作流状态
--   3. ticket 增加 tester_id（测试人员）
--   4. user_request 增加 request_no / affected_service / occurrence_time / requested_resolution_time
-- ============================================================

-- ---------- 1. ticket_history schema 对齐 ----------
-- V1.0.9 创建了 from_status/to_status/comment_text/ticket_type/username，
-- V1.0.11 的 CREATE TABLE IF NOT EXISTS 因表已存在而未生效，实体却映射 old_status/new_status/comment。
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS old_status VARCHAR(50);
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS new_status VARCHAR(50);
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS old_agent_id BIGINT;
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS new_agent_id BIGINT;
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS old_team_id BIGINT;
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS new_team_id BIGINT;
ALTER TABLE ticket_history ADD COLUMN IF NOT EXISTS comment TEXT;

-- 迁移旧列数据到新列（仅在新列为空时）
UPDATE ticket_history SET old_status = from_status WHERE old_status IS NULL AND from_status IS NOT NULL;
UPDATE ticket_history SET new_status = to_status WHERE new_status IS NULL AND to_status IS NOT NULL;
UPDATE ticket_history SET comment = comment_text WHERE comment IS NULL AND comment_text IS NOT NULL;

-- ---------- 2. ticket_status 迁移到新工作流 ----------
-- 旧状态: NEW, ASSIGNED, APPROVED, DISPATCHED, PENDING, RESOLVED, CLOSED, REJECTED
-- 新状态: NEW, ASSIGNED, IN_PROGRESS, TESTING, RESOLVED, USER_TEST_FAILED, CLOSED
UPDATE ticket SET ticket_status = 'ASSIGNED' WHERE ticket_status IN ('APPROVED', 'DISPATCHED');
UPDATE ticket SET ticket_status = 'IN_PROGRESS' WHERE ticket_status = 'PENDING';
UPDATE ticket SET ticket_status = 'CLOSED' WHERE ticket_status = 'REJECTED';

-- 同步 ticket_history 中的旧状态值
UPDATE ticket_history SET old_status = 'ASSIGNED' WHERE old_status IN ('APPROVED', 'DISPATCHED');
UPDATE ticket_history SET old_status = 'IN_PROGRESS' WHERE old_status = 'PENDING';
UPDATE ticket_history SET old_status = 'CLOSED' WHERE old_status = 'REJECTED';
UPDATE ticket_history SET new_status = 'ASSIGNED' WHERE new_status IN ('APPROVED', 'DISPATCHED');
UPDATE ticket_history SET new_status = 'IN_PROGRESS' WHERE new_status = 'PENDING';
UPDATE ticket_history SET new_status = 'CLOSED' WHERE new_status = 'REJECTED';

-- ---------- 3. ticket 增加 tester_id ----------
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS tester_id BIGINT;

-- ---------- 4. user_request 增加请求字段 ----------
ALTER TABLE user_request ADD COLUMN IF NOT EXISTS request_no VARCHAR(50);
ALTER TABLE user_request ADD COLUMN IF NOT EXISTS affected_service VARCHAR(255);
ALTER TABLE user_request ADD COLUMN IF NOT EXISTS occurrence_time TIMESTAMP;
ALTER TABLE user_request ADD COLUMN IF NOT EXISTS requested_resolution_time TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_user_request_request_no ON user_request(request_no);

-- 为已存在的 user_request 生成请求编号 REQ-YYYYMMDD-NNNN
UPDATE user_request ur
SET request_no = 'REQ-' || to_char(t.created_at, 'YYYYMMDD') || '-' || lpad(ur.id::text, 4, '0')
FROM ticket t
WHERE ur.id = t.id AND ur.request_no IS NULL;

COMMENT ON COLUMN ticket.tester_id IS 'IT internal tester assigned to verify the fix';
COMMENT ON COLUMN user_request.request_no IS 'Human-readable request reference, e.g. REQ-20260808-0001';
COMMENT ON COLUMN user_request.affected_service IS 'Free-text affected service / system reported by requester';
COMMENT ON COLUMN user_request.occurrence_time IS 'When the issue occurred (reported by requester)';
COMMENT ON COLUMN user_request.requested_resolution_time IS 'Requested resolution deadline (reported by requester)';
