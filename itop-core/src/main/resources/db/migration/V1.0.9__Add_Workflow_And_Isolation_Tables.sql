-- V1.0.9__Add_Workflow_And_Isolation_Tables.sql
-- 工单流转历史、工单日志、工单CI关联、用户可访问组织、站内通知、FAQ分类

-- ============================================================
-- 1. 工单流转历史 (ticket_history)
--    记录工单状态变更：谁、何时、从什么状态、通过什么动作、变成什么状态
-- ============================================================
CREATE TABLE ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    ticket_type VARCHAR(50) NOT NULL DEFAULT 'ticket',  -- ticket / problem / change_request
    action VARCHAR(50) NOT NULL,  -- create, assign, reassign, resolve, close, reopen, pending, resume, approve, reject, escalate
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    user_id BIGINT,
    username VARCHAR(100),
    comment_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_history_ticket ON ticket_history(ticket_id);
CREATE INDEX idx_ticket_history_type ON ticket_history(ticket_type);
CREATE INDEX idx_ticket_history_action ON ticket_history(action);
CREATE INDEX idx_ticket_history_created ON ticket_history(created_at);

-- ============================================================
-- 2. 工单日志 (ticket_log)
--    公共日志（用户可见）和内部日志（仅技术团队可见）和系统日志
-- ============================================================
CREATE TABLE ticket_log (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    ticket_type VARCHAR(50) NOT NULL DEFAULT 'ticket',  -- ticket / problem / change_request
    log_type VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',  -- PUBLIC / INTERNAL / SYSTEM
    message TEXT NOT NULL,
    user_id BIGINT,
    username VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_log_ticket ON ticket_log(ticket_id);
CREATE INDEX idx_ticket_log_type ON ticket_log(log_type);
CREATE INDEX idx_ticket_log_created ON ticket_log(created_at);

-- ============================================================
-- 3. 工单关联CI (ticket_ci)
--    通用工单-配置项关联，支持任意工单类型关联多个CI
-- ============================================================
CREATE TABLE ticket_ci (
    ticket_id BIGINT NOT NULL,
    ci_id BIGINT NOT NULL REFERENCES configuration_item(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) DEFAULT 'impacts',  -- impacts / depends_on / affects
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ticket_id, ci_id)
);

CREATE INDEX idx_ticket_ci_ticket ON ticket_ci(ticket_id);
CREATE INDEX idx_ticket_ci_ci ON ticket_ci(ci_id);

-- ============================================================
-- 4. 用户可访问组织 (user_accessible_org)
--    数据隔离核心：定义用户可以查看哪些组织的数据
-- ============================================================
CREATE TABLE user_accessible_org (
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    org_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    include_children BOOLEAN DEFAULT false,  -- 是否包含子组织
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, org_id)
);

CREATE INDEX idx_user_accessible_org_user ON user_accessible_org(user_id);
CREATE INDEX idx_user_accessible_org_org ON user_accessible_org(org_id);

-- ============================================================
-- 5. 站内通知 (notification)
-- ============================================================
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    notification_type VARCHAR(50) DEFAULT 'INFO',  -- INFO / WARNING / TICKET / APPROVAL / SLA / SYSTEM
    entity_type VARCHAR(50),
    entity_id BIGINT,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_user ON notification(user_id);
CREATE INDEX idx_notification_read ON notification(is_read);
CREATE INDEX idx_notification_created ON notification(created_at);

-- ============================================================
-- 6. FAQ 分类 (faq_category)
-- ============================================================
CREATE TABLE faq_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) UNIQUE,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_faq_category_code ON faq_category(code);
CREATE INDEX idx_faq_category_status ON faq_category(status);

-- 触发器
CREATE TRIGGER update_faq_category_updated_at BEFORE UPDATE ON faq_category
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_updated_at BEFORE UPDATE ON notification
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 种子数据
-- ============================================================

-- FAQ 分类
INSERT INTO faq_category (name, code, description, sort_order, status) VALUES
('账户管理', 'ACCOUNT', '账号、密码、权限相关问题', 1, 'active'),
('网络访问', 'NETWORK', 'VPN、网络连接、访问权限相关', 2, 'active'),
('支持服务', 'SUPPORT', 'IT支持、报障、服务请求', 3, 'active'),
('办公设备', 'DEVICE', '电脑、打印机、办公设备', 4, 'active'),
('业务系统', 'BUSINESS', '业务系统使用问题', 5, 'active')
ON CONFLICT (code) DO NOTHING;

-- 更新已有 FAQ 的分类字段为分类 ID 关联（保持兼容，faq.faq_category 仍为字符串）
-- 上述 faq_category 表供后续管理使用，faq 表的 faq_category 字段保持字符串兼容

-- 为 admin 用户添加全局可访问组织（信息中心 id=200，include_children=true 覆盖所有子组织）
-- 注意：ADMIN 角色默认全局权限，此数据作为示例
INSERT INTO user_accessible_org (user_id, org_id, include_children)
SELECT u.id, 100, true FROM "user" u WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;
