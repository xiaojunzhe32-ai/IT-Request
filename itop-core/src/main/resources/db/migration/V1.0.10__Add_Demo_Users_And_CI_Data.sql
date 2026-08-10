-- V1.0.10__Add_Demo_Users_And_CI_Data.sql
-- 补全多角色 demo 用户、CI 种子数据、用户可访问组织分配

-- ============================================================
-- 1. 补充角色定义
-- ============================================================
INSERT INTO role (name, role_code, status, is_system, permissions) VALUES
('组织管理员', 'ORG_ADMIN', 'active', true, '["org:read","org:write","user:read","user:write","team:read","ci:read","ticket:read","report:read"]'),
('普通提单用户', 'REQUESTER', 'active', true, '["ticket:create","ticket:read","knowledge:read"]'),
('部门联络员', 'DEPT_COORDINATOR', 'active', true, '["ticket:create","ticket:read","knowledge:read","report:read"]'),
('服务台坐席', 'SERVICE_DESK', 'active', true, '["ticket:read","ticket:create","ticket:assign","ticket:resolve","ticket:close","ticket:reopen","knowledge:read","ci:read","report:read"]'),
('技术处理人', 'SUPPORT_AGENT', 'active', true, '["ticket:read","ticket:resolve","ci:read","knowledge:read"]'),
('支持团队负责人', 'SUPPORT_LEAD', 'active', true, '["ticket:read","ticket:assign","ticket:resolve","ticket:close","ticket:reopen","team:read","ci:read","report:read"]'),
('问题经理', 'PROBLEM_MANAGER', 'active', true, '["problem:read","problem:write","ticket:read","ci:read","knowledge:read","knowledge:write"]'),
('变更经理', 'CHANGE_MANAGER', 'active', true, '["change:read","change:write","change:approve","ticket:read","ci:read","report:read"]'),
('CMDB管理员', 'CMDB_MANAGER', 'active', true, '["ci:read","ci:write","ci:delete","org:read","sla:read","sla:write"]'),
('知识管理员', 'KNOWLEDGE_MANAGER', 'active', true, '["knowledge:read","knowledge:write","knowledge:publish"]'),
('审计员', 'AUDITOR', 'active', true, '["audit:read","org:read","user:read","ticket:read","ci:read","report:read","report:export"]')
ON CONFLICT (role_code) DO NOTHING;

-- ============================================================
-- 2. Demo 用户（密码统一为 admin123）
-- BCrypt hash for 'admin123': $2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC
-- ============================================================
INSERT INTO "user" (id, name, username, password, email, first_name, last_name, phone, org_id, status, auth_method, language)
VALUES
(10, '服务台张三', 'servicedesk01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'servicedesk01@gov.example.com', '三', '张', '13800138010', 200, 'active', 'LOCAL', 'zh_CN'),
(11, '应用运维王五', 'appops01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'appops01@gov.example.com', '五', '王', '13800138011', 200, 'active', 'LOCAL', 'zh_CN'),
(12, '网络运维赵六', 'netops01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'netops01@gov.example.com', '六', '赵', '13800138012', 200, 'active', 'LOCAL', 'zh_CN'),
(13, '系统运维钱七', 'sysops01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'sysops01@gov.example.com', '七', '钱', '13800138013', 200, 'active', 'LOCAL', 'zh_CN'),
(14, '财务处用户', 'finance01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'finance01@gov.example.com', '财', '务', '13800138014', 108, 'active', 'LOCAL', 'zh_CN'),
(15, '人社局用户', 'hr01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'hr01@gov.example.com', '人', '社', '13800138015', 109, 'active', 'LOCAL', 'zh_CN'),
(16, '综合办联络员', 'office01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'office01@gov.example.com', '综', '合', '13800138016', 101, 'active', 'LOCAL', 'zh_CN'),
(17, '变更经理', 'change01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'change01@gov.example.com', '变', '更', '13800138017', 200, 'active', 'LOCAL', 'zh_CN'),
(18, '问题经理', 'problem01', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'problem01@gov.example.com', '问', '题', '13800138018', 200, 'active', 'LOCAL', 'zh_CN')
ON CONFLICT (username) DO NOTHING;

-- 分配角色
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'servicedesk01' AND r.role_code = 'SERVICE_DESK'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'appops01' AND r.role_code = 'SUPPORT_AGENT'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'netops01' AND r.role_code = 'SUPPORT_AGENT'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'sysops01' AND r.role_code = 'SUPPORT_AGENT'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'finance01' AND r.role_code = 'REQUESTER'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'hr01' AND r.role_code = 'REQUESTER'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'office01' AND r.role_code = 'DEPT_COORDINATOR'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'change01' AND r.role_code = 'CHANGE_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.username = 'problem01' AND r.role_code = 'PROBLEM_MANAGER'
ON CONFLICT DO NOTHING;

-- ============================================================
-- 3. 用户可访问组织分配
-- ============================================================
-- admin: 全局（org 100 某市人民政府，包含所有子组织）
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- 服务台: 可访问所有部门（org 100，包含子组织）
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'servicedesk01'
ON CONFLICT DO NOTHING;

-- 应用运维: 信息中心 + 所有业务部门
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 200, true FROM "user" u WHERE u.username = 'appops01'
ON CONFLICT DO NOTHING;
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'appops01'
ON CONFLICT DO NOTHING;

-- 网络运维: 同上
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 200, true FROM "user" u WHERE u.username = 'netops01'
ON CONFLICT DO NOTHING;
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'netops01'
ON CONFLICT DO NOTHING;

-- 系统运维: 同上
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 200, true FROM "user" u WHERE u.username = 'sysops01'
ON CONFLICT DO NOTHING;
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'sysops01'
ON CONFLICT DO NOTHING;

-- 普通用户: 只能访问自己所属组织
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, o.id, false FROM "user" u JOIN organization o ON u.org_id = o.id WHERE u.username = 'finance01'
ON CONFLICT DO NOTHING;

INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, o.id, false FROM "user" u JOIN organization o ON u.org_id = o.id WHERE u.username = 'hr01'
ON CONFLICT DO NOTHING;

INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, o.id, true FROM "user" u JOIN organization o ON u.org_id = o.id WHERE u.username = 'office01'
ON CONFLICT DO NOTHING;

-- 变更经理和问题经理: 全局
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'change01'
ON CONFLICT DO NOTHING;

INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'problem01'
ON CONFLICT DO NOTHING;

-- ============================================================
-- 4. CI 种子数据（服务器、应用、网络设备）
-- ============================================================

-- 应用: 政务审批系统
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (500, '政务审批系统', 200, 'Application', 'APPLICATION', 'active', 'high', '核心政务审批业务系统')
ON CONFLICT DO NOTHING;

INSERT INTO application (id, app_code, app_type, version, vendor, url)
VALUES (500, 'GOV_APPROVAL', 'WEBAPP', '3.2.1', '某软件公司', 'https://approval.gov.example.com')
ON CONFLICT DO NOTHING;

-- 应用: 财务报销系统
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (501, '财务报销系统', 200, 'Application', 'APPLICATION', 'active', 'high', '财务报销管理')
ON CONFLICT DO NOTHING;

INSERT INTO application (id, app_code, app_type, version, vendor, url)
VALUES (501, 'FIN_REIMBURSE', 'WEBAPP', '2.0.5', '某财务软件公司', 'https://finance.gov.example.com')
ON CONFLICT DO NOTHING;

-- 应用: 人事考勤系统
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (502, '人事考勤系统', 200, 'Application', 'APPLICATION', 'active', 'medium', '人事考勤管理')
ON CONFLICT DO NOTHING;

INSERT INTO application (id, app_code, app_type, version, vendor, url)
VALUES (502, 'HR_ATTEND', 'WEBAPP', '1.8.0', '某HR软件公司', 'https://hr.gov.example.com')
ON CONFLICT DO NOTHING;

-- 服务器: app-server-01
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (600, 'app-server-01', 200, 'Server', 'SERVER', 'active', 'high', '应用服务器1')
ON CONFLICT DO NOTHING;

INSERT INTO physical_device (id, brand_name, model_name, serial_number, asset_tag)
VALUES (600, 'Dell', 'PowerEdge R750', 'SRV20240001', 'AST-600')
ON CONFLICT DO NOTHING;

INSERT INTO server (id, cpu, ram, disk, os_family, os_version, ip_address, is_virtual, server_type)
VALUES (600, 'Intel Xeon 16C', '64GB', '2TB SSD', 'Linux', 'CentOS 8', '192.168.1.10', false, 'PHYSICAL')
ON CONFLICT DO NOTHING;

-- 服务器: app-server-02
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (601, 'app-server-02', 200, 'Server', 'SERVER', 'active', 'high', '应用服务器2')
ON CONFLICT DO NOTHING;

INSERT INTO physical_device (id, brand_name, model_name, serial_number, asset_tag)
VALUES (601, 'Dell', 'PowerEdge R750', 'SRV20240002', 'AST-601')
ON CONFLICT DO NOTHING;

INSERT INTO server (id, cpu, ram, disk, os_family, os_version, ip_address, is_virtual, server_type)
VALUES (601, 'Intel Xeon 16C', '64GB', '2TB SSD', 'Linux', 'CentOS 8', '192.168.1.11', false, 'PHYSICAL')
ON CONFLICT DO NOTHING;

-- 服务器: db-server-01
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (602, 'db-server-01', 200, 'Server', 'SERVER', 'active', 'critical', '数据库服务器')
ON CONFLICT DO NOTHING;

INSERT INTO physical_device (id, brand_name, model_name, serial_number, asset_tag)
VALUES (602, 'HP', 'ProLiant DL380', 'SRV20240003', 'AST-602')
ON CONFLICT DO NOTHING;

INSERT INTO server (id, cpu, ram, disk, os_family, os_version, ip_address, is_virtual, server_type)
VALUES (602, 'Intel Xeon 32C', '128GB', '4TB SSD', 'Linux', 'CentOS 8', '192.168.1.20', false, 'PHYSICAL')
ON CONFLICT DO NOTHING;

-- 网络设备: core-switch-01
INSERT INTO configuration_item (id, name, org_id, final_class, ci_type, status, business_criticity, description)
VALUES (700, 'core-switch-01', 200, 'NetworkDevice', 'NETWORK_DEVICE', 'active', 'critical', '核心交换机')
ON CONFLICT DO NOTHING;

INSERT INTO physical_device (id, brand_name, model_name, serial_number, asset_tag)
VALUES (700, 'Cisco', 'Catalyst 9300', 'NET20240001', 'AST-700')
ON CONFLICT DO NOTHING;

INSERT INTO network_device (id, device_type, ip_address, mac_address, port_count, firmware_version)
VALUES (700, 'SWITCH', '192.168.1.1', '00:1A:2B:3C:4D:5E', 48, '16.12.4')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 5. CI 关系
-- ============================================================
INSERT INTO lnk_ci_relation (name, source_ci_id, target_ci_id, relation_type, relation_strength, status)
VALUES
('政务审批系统 runs_on app-server-01', 500, 600, 'runs_on', 'high', 'active'),
('财务报销系统 runs_on app-server-02', 501, 601, 'runs_on', 'high', 'active'),
('人事考勤系统 runs_on app-server-01', 502, 600, 'runs_on', 'medium', 'active'),
('app-server-01 depends_on db-server-01', 600, 602, 'depends_on', 'high', 'active'),
('app-server-02 depends_on db-server-01', 601, 602, 'depends_on', 'high', 'active'),
('app-server-01 connects_to core-switch-01', 600, 700, 'connects_to', 'high', 'active'),
('app-server-02 connects_to core-switch-01', 601, 700, 'connects_to', 'high', 'active')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 6. 更新工单关联CI和服务
-- ============================================================
-- 为现有工单关联CI
INSERT INTO ticket_ci (ticket_id, ci_id, relation_type)
VALUES
(10001, 700, 'affects'),  -- VPN问题关联核心交换机
(10003, 500, 'affects'),  -- OA系统慢关联政务审批系统
(10005, 700, 'affects')   -- 网络中断关联核心交换机
ON CONFLICT DO NOTHING;

-- 更新序列（使用 pg_get_serial_sequence 安全获取序列名）
SELECT setval(pg_get_serial_sequence('"user"', 'id'), GREATEST((SELECT MAX(id) FROM "user"), 1));
SELECT setval(pg_get_serial_sequence('configuration_item', 'id'), GREATEST((SELECT MAX(id) FROM configuration_item), 1));
