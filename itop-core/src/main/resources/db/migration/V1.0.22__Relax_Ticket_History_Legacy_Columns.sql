-- ============================================================
-- V1.0.22: 放宽 ticket_history 遗留列的 NOT NULL 约束
--
-- 背景：ticket_history 有两套状态列：
--   V1.0.9 创建 from_status / to_status (to_status NOT NULL) / comment_text
--   V1.0.11 追加 old_status / new_status / comment（实体 TicketHistory 映射这套）
-- 实体插入时不写 to_status，触发 NOT NULL 违约。V1.0.20 已把旧列数据迁移到新列，
-- 此迁移放宽旧列约束，避免插入失败（旧列保留以兼容历史读取）。
-- ============================================================

ALTER TABLE ticket_history ALTER COLUMN to_status DROP NOT NULL;
ALTER TABLE ticket_history ALTER COLUMN from_status DROP NOT NULL;
