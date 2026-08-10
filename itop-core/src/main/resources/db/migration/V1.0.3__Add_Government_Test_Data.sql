-- 政府组织测试数据
-- 创建政府机构组织架构

-- 添加唯一索引以支持 ON CONFLICT 子句（organization.code 和 contact.email 在 V1.0.0 建表时未加唯一约束）
CREATE UNIQUE INDEX IF NOT EXISTS idx_org_code_unique ON organization(code);
CREATE UNIQUE INDEX IF NOT EXISTS idx_contact_email_unique ON contact(email);

-- 修复 ticket.team_id 外键约束
-- V1.0.1 创建 ticket 时 team_id 为无 FK 的 BIGINT，V1.0.2 的 ADD COLUMN IF NOT EXISTS 因列已存在而跳过，FK 从未添加
DO $$ BEGIN
    ALTER TABLE ticket ADD CONSTRAINT fk_ticket_team FOREIGN KEY (team_id) REFERENCES team(id);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- 插入主组织（假设organization表为空或只有测试数据）
INSERT INTO organization (id, name, code, type, status, created_at, updated_at)
VALUES (100, '某市人民政府', 'GOV_CITY', 'GOVERNMENT', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 下属部门
INSERT INTO organization (id, name, code, type, parent_id, status, created_at, updated_at)
VALUES
(101, '市政府办公室', 'GOV_OFFICE', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(102, '发展和改革委员会', 'GOV_DRC', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(103, '教育局', 'GOV_EDU', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(104, '科学技术局', 'GOV_SCI', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(105, '工业和信息化局', 'GOV_IIT', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(106, '公安局', 'GOV_POLICE', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(107, '民政局', 'GOV_CIVIL', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(108, '财政局', 'GOV_FINANCE', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(109, '人力资源和社会保障局', 'GOV_HR', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(110, '自然资源局', 'GOV_NATURAL', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(111, '生态环境局', 'GOV_ENV', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(112, '住房和城乡建设局', 'GOV_HOUSING', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(113, '交通运输局', 'GOV_TRANSPORT', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(114, '水务局', 'GOV_WATER', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(115, '农业农村局', 'GOV_AGRIC', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(116, '商务局', 'GOV_COMMERCE', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(117, '文化和旅游局', 'GOV_CULTURE', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(118, '卫生健康委员会', 'GOV_HEALTH', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(119, '应急管理局', 'GOV_EMERGENCY', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(120, '审计局', 'GOV_AUDIT', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(121, '市场监督管理局', 'GOV_MARKET', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(122, '统计局', 'GOV_STATS', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(123, '林业局', 'GOV_FOREST', 'DEPARTMENT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 信息中心（IT部门，作为技术支持方）
INSERT INTO organization (id, name, code, type, parent_id, status, created_at, updated_at)
VALUES (200, '信息中心', 'IT_CENTER', 'IT_DEPT', 100, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 团队数据（已在V1.0.2中插入部分，这里补充更多）
INSERT INTO team (id, name, org_id, team_code, team_type, email, phone, status, created_at, updated_at)
VALUES
(1, '服务台一线', 200, 'HELPDESK_L1', 'HELPDESK', 'helpdesk@gov.example.com', '400-800-1001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '应用运维组', 200, 'APP_SUPPORT', 'SUPPORT', 'app-support@gov.example.com', '400-800-1002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '网络运维组', 200, 'NETWORK_SUPPORT', 'SUPPORT', 'network@gov.example.com', '400-800-1003', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '基础设施组', 200, 'INFRA_SUPPORT', 'SUPPORT', 'infra@gov.example.com', '400-800-1004', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '安全运维组', 200, 'SECURITY_SUPPORT', 'SUPPORT', 'security@gov.example.com', '400-800-1005', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '数据管理组', 200, 'DATA_SUPPORT', 'SUPPORT', 'data@gov.example.com', '400-800-1006', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (team_code) DO NOTHING;

-- 创建联系人（Person）
-- person 表通过 id 引用 contact(id)，公共字段（name/email/phone/status/org_id）在 contact 表
-- 必须先插 contact 获得 id，再插 person 扩展字段
INSERT INTO contact (id, name, email, phone, status, org_id, contact_type, created_at, updated_at)
VALUES
(1001, '张三', 'zhangsan@gov.example.com', '13800138001', 'active', 200, 'Person', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1002, '李四', 'lisi@gov.example.com', '13800138002', 'active', 200, 'Person', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1003, '王五', 'wangwu@gov.example.com', '13800138003', 'active', 200, 'Person', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1004, '赵六', 'zhaoliu@gov.example.com', '13800138004', 'active', 200, 'Person', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1005, '钱七', 'qianqi@gov.example.com', '13800138005', 'active', 200, 'Person', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1006, '孙八', 'sunba@gov.example.com', '13800138006', 'active', 200, 'Person', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO person (id, first_name, last_name)
VALUES
(1001, '三', '张'),
(1002, '四', '李'),
(1003, '五', '王'),
(1004, '六', '赵'),
(1005, '七', '钱'),
(1006, '八', '孙')
ON CONFLICT (id) DO NOTHING;

-- 团队成员关系
INSERT INTO team_member (team_id, person_id)
VALUES
(1, 1001), -- 张三 - 服务台
(1, 1002), -- 李四 - 服务台
(2, 1003), -- 王五 - 应用运维
(3, 1004), -- 赵六 - 网络运维
(4, 1005), -- 钱七 - 基础设施
(5, 1006)  -- 孙八 - 安全运维
ON CONFLICT DO NOTHING;

-- 示例工单数据
-- 注意：工单流转状态写入 ticket_status 列，不是 status 列（status 是通用实体状态，默认 active）
-- ticket_type 为 NOT NULL，必须提供
INSERT INTO ticket (id, name, title, description, org_id, caller_id, team_id, ticket_status, impact, urgency, priority, final_class, ticket_type, start_date, last_update_date, created_at, updated_at)
VALUES
(10001, 'VPN连接问题', 'VPN连接问题', '财政局工作人员反映无法连接VPN，提示认证失败', 108, 1001, 3, 'ASSIGNED', '2', '2', '2', 'UserRequest', 'request', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10002, '邮箱无法登录', '邮箱无法登录', '教育局多人反映企业邮箱无法登录，提示密码错误', 103, 1002, 1, 'NEW', '2', '3', '3', 'UserRequest', 'request', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10003, 'OA系统访问慢', 'OA系统访问缓慢', 'OA系统访问速度非常慢，影响办公效率', 109, 1003, 2, 'ASSIGNED', '2', '2', '2', 'Incident', 'incident', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10004, '打印机故障', '打印机无法打印', '发改委办公室打印机无法打印，卡纸', 102, 1004, NULL, 'NEW', '3', '3', '3', 'UserRequest', 'request', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10005, '网络中断', '网络完全中断', '应急管理局整个办公室网络中断，无法访问任何系统', 119, 1005, 3, 'RESOLVED', '1', '1', '1', 'Incident', 'incident', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 更新序列
SELECT setval('organization_id_seq', (SELECT MAX(id) FROM organization));
SELECT setval('team_id_seq', (SELECT MAX(id) FROM team));
SELECT setval('contact_id_seq', (SELECT MAX(id) FROM contact));
SELECT setval('ticket_id_seq', (SELECT MAX(id) FROM ticket));
