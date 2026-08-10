-- ============================================================
-- V1.0.19: 请求自动路由规则表 (routing_rule)
--   新建请求时按 sort_order 顺序匹配第一条命中的规则，
--   将请求分配到 team_id 指向的团队；无命中时回退到兜底规则。
-- ============================================================

CREATE TABLE routing_rule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    org_id BIGINT REFERENCES organization(id),
    request_type VARCHAR(100),
    priority VARCHAR(20),
    team_id BIGINT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 100,
    is_fallback BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_routing_rule_enabled ON routing_rule(enabled);
CREATE INDEX idx_routing_rule_sort ON routing_rule(sort_order);
CREATE INDEX idx_routing_rule_org ON routing_rule(org_id);
CREATE INDEX idx_routing_rule_team ON routing_rule(team_id);

CREATE TRIGGER update_routing_rule_updated_at BEFORE UPDATE ON routing_rule
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 种子数据：一条兜底规则 + 几条示例规则
-- team: 1=服务台一线, 2=应用运维组, 3=网络运维组, 4=基础设施组, 5=安全运维组, 6=数据管理组
-- 兜底规则：所有未命中的请求分配到服务台一线（team_id=1）
INSERT INTO routing_rule (name, description, team_id, enabled, sort_order, is_fallback)
VALUES ('Default Fallback', 'Catch-all rule that routes unmatched requests to the helpdesk.', 1, true, 9999, true);

-- 示例：网络类问题 -> 网络运维团队（team_id=3）
INSERT INTO routing_rule (name, description, request_type, team_id, enabled, sort_order, is_fallback)
SELECT 'Network Issues', 'Route network issues to the network operations team.', 'Network Issue', 3, true, 10, false
WHERE EXISTS (SELECT 1 FROM team WHERE id = 3);

-- 示例：账号访问类问题 -> 应用运维团队（team_id=2）
INSERT INTO routing_rule (name, description, request_type, team_id, enabled, sort_order, is_fallback)
SELECT 'Account Access', 'Route account access requests to the application operations team.', 'Account Access', 2, true, 20, false
WHERE EXISTS (SELECT 1 FROM team WHERE id = 2);

-- 示例：硬件类问题 -> 基础设施团队（team_id=4）
INSERT INTO routing_rule (name, description, request_type, team_id, enabled, sort_order, is_fallback)
SELECT 'Hardware Issues', 'Route hardware issues to the infrastructure team.', 'Hardware Issue', 4, true, 30, false
WHERE EXISTS (SELECT 1 FROM team WHERE id = 4);
