-- V1.0.4__Add_Service_Catalog_And_SLA.sql
-- Service Catalog and SLA schema

-- Service Family (服务系列)
CREATE TABLE service_family (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    code VARCHAR(100) UNIQUE NOT NULL,
    icon VARCHAR(50),
    sort_order INTEGER DEFAULT 0
);

CREATE INDEX idx_service_family_code ON service_family(code);
CREATE INDEX idx_service_family_status ON service_family(status);

-- Service Subfamily (服务子类)
CREATE TABLE service_subfamily (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    code VARCHAR(100) UNIQUE NOT NULL,
    family_id BIGINT NOT NULL REFERENCES service_family(id),
    sort_order INTEGER DEFAULT 0
);

CREATE INDEX idx_service_subfamily_family ON service_subfamily(family_id);
CREATE INDEX idx_service_subfamily_code ON service_subfamily(code);

-- Service (服务项)
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    code VARCHAR(100) UNIQUE NOT NULL,
    subfamily_id BIGINT NOT NULL REFERENCES service_subfamily(id),
    org_id BIGINT REFERENCES organization(id),
    service_type VARCHAR(50) DEFAULT 'USER_REQUEST',
    sla_id BIGINT,  -- Reference to SLA
    sort_order INTEGER DEFAULT 0
);

CREATE INDEX idx_service_subfamily ON service(subfamily_id);
CREATE INDEX idx_service_code ON service(code);
CREATE INDEX idx_service_org ON service(org_id);
CREATE INDEX idx_service_type ON service(service_type);

-- SLA (Service Level Agreement)
CREATE TABLE sla (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    code VARCHAR(100) UNIQUE NOT NULL,
    org_id BIGINT REFERENCES organization(id),
    tto_hours INTEGER DEFAULT 4,  -- Time to Own (hours)
    ttr_hours INTEGER DEFAULT 8,  -- Time to Resolve (hours)
    priority VARCHAR(20) DEFAULT 'medium',
    calendar_id VARCHAR(50),  -- Reference to calendar (working hours)
    is_default BOOLEAN DEFAULT false
);

CREATE INDEX idx_sla_code ON sla(code);
CREATE INDEX idx_sla_org ON sla(org_id);
CREATE INDEX idx_sla_priority ON sla(priority);
CREATE INDEX idx_sla_default ON sla(is_default);

-- SLA Priority Matrix (优先级矩阵)
CREATE TABLE sla_priority_matrix (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    sla_id BIGINT NOT NULL REFERENCES sla(id),
    urgency VARCHAR(20) NOT NULL,  -- low, medium, high, critical
    impact VARCHAR(20) NOT NULL,   -- low, medium, high, critical
    priority VARCHAR(20) NOT NULL, -- low, medium, high, critical
    tto_hours INTEGER,
    ttr_hours INTEGER
);

CREATE INDEX idx_sla_matrix_sla ON sla_priority_matrix(sla_id);
CREATE INDEX idx_sla_matrix_urgency ON sla_priority_matrix(urgency);
CREATE INDEX idx_sla_matrix_impact ON sla_priority_matrix(impact);

-- Update Ticket table to link with Service and SLA
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS service_id BIGINT REFERENCES service(id);
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS sla_id BIGINT REFERENCES sla(id);
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS sla_deadline TIMESTAMP;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS sla_tto_deadline TIMESTAMP;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS sla_ttr_deadline TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_ticket_service ON ticket(service_id);
CREATE INDEX IF NOT EXISTS idx_ticket_sla ON ticket(sla_id);

-- 修复 ticket.service_id 外键约束
-- V1.0.1 创建 ticket 时 service_id 为无 FK 的 BIGINT，上面的 ADD COLUMN IF NOT EXISTS 因列已存在而跳过
DO $$ BEGIN
    ALTER TABLE ticket ADD CONSTRAINT fk_ticket_service FOREIGN KEY (service_id) REFERENCES service(id);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Insert default SLA
INSERT INTO sla (name, code, tto_hours, ttr_hours, priority, is_default, status)
VALUES
('Default SLA - Low', 'SLA_LOW', 8, 24, 'low', false, 'active'),
('Default SLA - Medium', 'SLA_MEDIUM', 4, 8, 'medium', true, 'active'),
('Default SLA - High', 'SLA_HIGH', 2, 4, 'high', false, 'active'),
('Default SLA - Critical', 'SLA_CRITICAL', 1, 2, 'critical', false, 'active');

-- Insert default Service Families
INSERT INTO service_family (name, code, description, icon, sort_order, status) VALUES
('IT Services', 'IT_SERVICES', 'Information Technology Services', 'Monitor', 1, 'active'),
('HR Services', 'HR_SERVICES', 'Human Resources Services', 'User', 2, 'active'),
('Facilities', 'FACILITIES', 'Facilities and Infrastructure Services', 'OfficeBuilding', 3, 'active'),
('Admin Services', 'ADMIN_SERVICES', 'Administrative Services', 'Document', 4, 'active');

-- Insert default Service Subfamilies
INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Hardware Support', 'HW_SUPPORT', id, 1, 'active' FROM service_family WHERE code = 'IT_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Software Support', 'SW_SUPPORT', id, 2, 'active' FROM service_family WHERE code = 'IT_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Network Support', 'NET_SUPPORT', id, 3, 'active' FROM service_family WHERE code = 'IT_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Account Management', 'ACC_MGMT', id, 1, 'active' FROM service_family WHERE code = 'IT_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Employee Onboarding', 'EMP_ONBOARD', id, 1, 'active' FROM service_family WHERE code = 'HR_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Employee Offboarding', 'EMP_OFFBOARD', id, 2, 'active' FROM service_family WHERE code = 'HR_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Leave Management', 'LEAVE_MGMT', id, 3, 'active' FROM service_family WHERE code = 'HR_SERVICES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Office Equipment', 'OFFICE_EQUIP', id, 1, 'active' FROM service_family WHERE code = 'FACILITIES';

INSERT INTO service_subfamily (name, code, family_id, sort_order, status)
SELECT 'Meeting Rooms', 'MEETING_ROOM', id, 2, 'active' FROM service_family WHERE code = 'FACILITIES';

-- Insert default Services
INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'Desktop Support', 'DESKTOP_SUPPORT', sf.id, 'USER_REQUEST',
       (SELECT id FROM sla WHERE code = 'SLA_MEDIUM'), 1, 'active'
FROM service_subfamily sf WHERE sf.code = 'HW_SUPPORT';

INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'Laptop Request', 'LAPTOP_REQUEST', sf.id, 'USER_REQUEST',
       (SELECT id FROM sla WHERE code = 'SLA_MEDIUM'), 2, 'active'
FROM service_subfamily sf WHERE sf.code = 'HW_SUPPORT';

INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'Software Installation', 'SW_INSTALL', sf.id, 'USER_REQUEST',
       (SELECT id FROM sla WHERE code = 'SLA_MEDIUM'), 1, 'active'
FROM service_subfamily sf WHERE sf.code = 'SW_SUPPORT';

INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'Email Account', 'EMAIL_ACCOUNT', sf.id, 'USER_REQUEST',
       (SELECT id FROM sla WHERE code = 'SLA_HIGH'), 1, 'active'
FROM service_subfamily sf WHERE sf.code = 'ACC_MGMT';

INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'Password Reset', 'PWD_RESET', sf.id, 'USER_REQUEST',
       (SELECT id FROM sla WHERE code = 'SLA_HIGH'), 2, 'active'
FROM service_subfamily sf WHERE sf.code = 'ACC_MGMT';

INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'Network Issue', 'NET_ISSUE', sf.id, 'INCIDENT',
       (SELECT id FROM sla WHERE code = 'SLA_HIGH'), 1, 'active'
FROM service_subfamily sf WHERE sf.code = 'NET_SUPPORT';

INSERT INTO service (name, code, subfamily_id, service_type, sla_id, sort_order, status)
SELECT 'VPN Access', 'VPN_ACCESS', sf.id, 'USER_REQUEST',
       (SELECT id FROM sla WHERE code = 'SLA_MEDIUM'), 2, 'active'
FROM service_subfamily sf WHERE sf.code = 'NET_SUPPORT';

-- Insert SLA Priority Matrix entries
INSERT INTO sla_priority_matrix (sla_id, urgency, impact, priority, tto_hours, ttr_hours, name, status)
SELECT id, 'critical', 'critical', 'critical', 1, 2, 'Critical-Urgency-Critical-Impact', 'active' FROM sla WHERE code = 'SLA_CRITICAL';

INSERT INTO sla_priority_matrix (sla_id, urgency, impact, priority, tto_hours, ttr_hours, name, status)
SELECT id, 'high', 'high', 'high', 2, 4, 'High-Urgency-High-Impact', 'active' FROM sla WHERE code = 'SLA_HIGH';

INSERT INTO sla_priority_matrix (sla_id, urgency, impact, priority, tto_hours, ttr_hours, name, status)
SELECT id, 'medium', 'medium', 'medium', 4, 8, 'Medium-Urgency-Medium-Impact', 'active' FROM sla WHERE code = 'SLA_MEDIUM';

INSERT INTO sla_priority_matrix (sla_id, urgency, impact, priority, tto_hours, ttr_hours, name, status)
SELECT id, 'low', 'low', 'low', 8, 24, 'Low-Urgency-Low-Impact', 'active' FROM sla WHERE code = 'SLA_LOW';

-- Triggers for updated_at
CREATE TRIGGER update_service_family_updated_at BEFORE UPDATE ON service_family
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_service_subfamily_updated_at BEFORE UPDATE ON service_subfamily
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_service_updated_at BEFORE UPDATE ON service
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sla_updated_at BEFORE UPDATE ON sla
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();