-- ============================================================
-- V1.0.21: 解除 ticket.caller_id / agent_id 对 contact/person 的外键约束
--
-- 背景：新请求工作流中，请求人(caller)、处理人(agent)、测试人(tester) 均为系统用户(User)，
-- 但 ticket.caller_id 原先外键引用 contact(id)，ticket.agent_id 引用 person(id)，
-- 导致 caller_id/agent_id 存放 user id 时违反外键约束。
-- 此迁移解除这两条外键，使 caller_id/agent_id/tester_id 统一存放 user id。
-- ============================================================

ALTER TABLE ticket DROP CONSTRAINT IF EXISTS ticket_caller_id_fkey;
ALTER TABLE ticket DROP CONSTRAINT IF EXISTS ticket_agent_id_fkey;

COMMENT ON COLUMN ticket.caller_id IS 'Requester user id (references user table, FK dropped to allow user ids)';
COMMENT ON COLUMN ticket.agent_id IS 'Assignee user id (references user table, FK dropped to allow user ids)';
COMMENT ON COLUMN ticket.tester_id IS 'Tester user id (references user table)';
