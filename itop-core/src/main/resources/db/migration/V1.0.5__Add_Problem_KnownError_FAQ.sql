-- V1.0.5__Add_Problem_KnownError_FAQ.sql
-- Problem Management, Known Error, and FAQ schema

-- Problem (问题管理)
CREATE TABLE problem (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Ticket fields (from ticket table via JOINED inheritance)
    org_id BIGINT REFERENCES organization(id),
    caller_id BIGINT REFERENCES contact(id),
    agent_id BIGINT REFERENCES person(id),
    team_id BIGINT REFERENCES team(id),
    service_id BIGINT REFERENCES service(id),
    request_type_id BIGINT,
    title VARCHAR(255) NOT NULL,
    impact VARCHAR(20) DEFAULT '2',
    urgency VARCHAR(20) DEFAULT '2',
    priority VARCHAR(20) DEFAULT '2',
    ticket_status VARCHAR(50) DEFAULT 'NEW',
    resolution VARCHAR(50) DEFAULT 'NONE',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    last_update_date TIMESTAMP,
    close_date TIMESTAMP,
    solution TEXT,
    final_class VARCHAR(100) NOT NULL DEFAULT 'Problem',
    ticket_type VARCHAR(50) DEFAULT 'problem',

    -- Problem specific fields
    problem_number VARCHAR(50) UNIQUE NOT NULL,
    problem_type VARCHAR(50) DEFAULT 'SOFTWARE',  -- SOFTWARE, HARDWARE, NETWORK, PROCESS
    root_cause TEXT,
    work_around TEXT,
    impact_analysis TEXT,
    related_change_id BIGINT
);

CREATE INDEX idx_problem_number ON problem(problem_number);
CREATE INDEX idx_problem_status ON problem(ticket_status);
CREATE INDEX idx_problem_org ON problem(org_id);
CREATE INDEX idx_problem_team ON problem(team_id);
CREATE INDEX idx_problem_agent ON problem(agent_id);

-- Known Error (已知错误)
CREATE TABLE known_error (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    error_code VARCHAR(100) UNIQUE NOT NULL,
    problem_id BIGINT REFERENCES problem(id),
    symptoms TEXT NOT NULL,
    cause TEXT,
    workaround TEXT,
    solution TEXT,
    error_type VARCHAR(50) DEFAULT 'SOFTWARE',  -- SOFTWARE, HARDWARE, NETWORK, CONFIGURATION
    severity VARCHAR(20) DEFAULT 'medium',  -- low, medium, high, critical
    apply_to_all BOOLEAN DEFAULT false,
    first_detected TIMESTAMP,
    last_occurrence TIMESTAMP,
    occurrence_count INTEGER DEFAULT 0
);

CREATE INDEX idx_known_error_code ON known_error(error_code);
CREATE INDEX idx_known_error_problem ON known_error(problem_id);
CREATE INDEX idx_known_error_type ON known_error(error_type);
CREATE INDEX idx_known_error_severity ON known_error(severity);

-- FAQ (常见问题)
CREATE TABLE faq (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    faq_category VARCHAR(100),
    service_id BIGINT REFERENCES service(id),
    keywords VARCHAR(500),
    view_count INTEGER DEFAULT 0,
    helpful_count INTEGER DEFAULT 0,
    not_helpful_count INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    is_published BOOLEAN DEFAULT true,
    author_id BIGINT REFERENCES person(id)
);

CREATE INDEX idx_faq_category ON faq(faq_category);
CREATE INDEX idx_faq_service ON faq(service_id);
CREATE INDEX idx_faq_published ON faq(is_published);
CREATE INDEX idx_faq_keywords ON faq(keywords);

-- Link KnownError to Incidents/UserRequests
CREATE TABLE known_error_ticket (
    known_error_id BIGINT NOT NULL REFERENCES known_error(id),
    ticket_id BIGINT NOT NULL REFERENCES ticket(id),
    PRIMARY KEY (known_error_id, ticket_id)
);

-- Insert default FAQ categories
INSERT INTO faq (name, question, answer, faq_category, sort_order, is_published, status) VALUES
('如何重置密码?', '如何重置密码?', '请访问登录页面，点击"忘记密码"链接，输入您的邮箱地址，系统将发送重置链接到您的邮箱。', '账户管理', 1, true, 'active'),
('如何申请VPN访问?', '如何申请VPN访问?', '请通过服务目录提交VPN访问申请，填写申请理由和访问时长，IT部门将在1-2个工作日内处理。', '网络访问', 2, true, 'active'),
('如何报告IT问题?', '如何报告IT问题?', '您可以通过以下方式报告IT问题：1. 登录IT门户提交工单；2. 发送邮件到support@company.com；3. 拨打服务热线。', '支持服务', 3, true, 'active');

-- Triggers for updated_at
CREATE TRIGGER update_problem_updated_at BEFORE UPDATE ON problem
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_known_error_updated_at BEFORE UPDATE ON known_error
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_faq_updated_at BEFORE UPDATE ON faq
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();