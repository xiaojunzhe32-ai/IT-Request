-- V1.0.0__Initial_Schema.sql
-- Initial database schema for iTop Java

-- Organization table
CREATE TABLE organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    code VARCHAR(255),
    parent_id BIGINT REFERENCES organization(id),
    type VARCHAR(100) DEFAULT 'company',
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    website VARCHAR(50),
    org_type VARCHAR(50) DEFAULT 'company'
);

CREATE INDEX idx_org_parent ON organization(parent_id);
CREATE INDEX idx_org_status ON organization(status);

-- Contact base table
CREATE TABLE contact (
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
    location_id BIGINT,
    phone VARCHAR(50),
    email VARCHAR(100),
    notify_email BOOLEAN DEFAULT true,
    notify_sms BOOLEAN DEFAULT false,
    contact_type VARCHAR(50) NOT NULL
);

CREATE INDEX idx_contact_org ON contact(org_id);
CREATE INDEX idx_contact_type ON contact(contact_type);

-- Person table
CREATE TABLE person (
    id BIGINT PRIMARY KEY REFERENCES contact(id),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    function VARCHAR(50),
    manager VARCHAR(100),
    manager_id BIGINT REFERENCES person(id)
);

CREATE INDEX idx_person_manager ON person(manager_id);

-- Configuration Item base table
CREATE TABLE configuration_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    org_id BIGINT NOT NULL REFERENCES organization(id),
    final_class VARCHAR(100) NOT NULL,
    asset_number VARCHAR(100),
    move2production TIMESTAMP,
    location_id BIGINT,
    contact_id BIGINT REFERENCES contact(id),
    obsolescence_date TIMESTAMP,
    business_criticity VARCHAR(50) DEFAULT 'medium',
    redundancy VARCHAR(50) DEFAULT 'no',
    documents_list TEXT,
    services_list TEXT,
    tickets_list TEXT,
    ci_type VARCHAR(50) NOT NULL
);

CREATE INDEX idx_ci_org ON configuration_item(org_id);
CREATE INDEX idx_ci_status ON configuration_item(status);
CREATE INDEX idx_ci_type ON configuration_item(ci_type);
CREATE INDEX idx_ci_final_class ON configuration_item(final_class);

-- Physical Device table
CREATE TABLE physical_device (
    id BIGINT PRIMARY KEY REFERENCES configuration_item(id),
    brand_id BIGINT,
    brand_name VARCHAR(100),
    model_id BIGINT,
    model_name VARCHAR(100),
    serial_number VARCHAR(100),
    asset_tag VARCHAR(100),
    purchase_date TIMESTAMP,
    warranty_end TIMESTAMP,
    power VARCHAR(50),
    rack_id BIGINT,
    rack_unit INTEGER
);

CREATE INDEX idx_device_serial ON physical_device(serial_number);
CREATE INDEX idx_device_asset_tag ON physical_device(asset_tag);

-- Server table
CREATE TABLE server (
    id BIGINT PRIMARY KEY REFERENCES physical_device(id),
    cpu VARCHAR(100),
    ram VARCHAR(50),
    disk VARCHAR(50),
    os_family VARCHAR(50),
    os_version VARCHAR(50),
    ip_address VARCHAR(50),
    mac_address VARCHAR(50),
    management_ip VARCHAR(50),
    virtual_host_id BIGINT,
    is_virtual BOOLEAN DEFAULT false,
    server_type VARCHAR(50) DEFAULT 'PHYSICAL'
);

CREATE INDEX idx_server_ip ON server(ip_address);
CREATE INDEX idx_server_mac ON server(mac_address);

-- Network Device table
CREATE TABLE network_device (
    id BIGINT PRIMARY KEY REFERENCES physical_device(id),
    device_type VARCHAR(50) DEFAULT 'SWITCH',
    ip_address VARCHAR(50),
    mac_address VARCHAR(50),
    management_ip VARCHAR(50),
    vlan_list TEXT,
    port_count INTEGER,
    firmware_version VARCHAR(50)
);

CREATE INDEX idx_netdev_ip ON network_device(ip_address);

-- Application table
CREATE TABLE application (
    id BIGINT PRIMARY KEY REFERENCES configuration_item(id),
    app_code VARCHAR(50),
    app_type VARCHAR(50) DEFAULT 'WEBAPP',
    version VARCHAR(50),
    vendor VARCHAR(100),
    license_type VARCHAR(50),
    license_expire TIMESTAMP,
    url VARCHAR(255),
    database_server_id BIGINT,
    web_server_id BIGINT,
    documentation_url VARCHAR(255)
);

CREATE INDEX idx_app_code ON application(app_code);

-- User table (quoted because "user" is a PostgreSQL reserved keyword)
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    org_id BIGINT REFERENCES organization(id),
    language VARCHAR(10) DEFAULT 'zh_CN',
    auth_method VARCHAR(20) DEFAULT 'LOCAL',
    last_login TIMESTAMP,
    failed_logins INTEGER DEFAULT 0,
    locked BOOLEAN DEFAULT false,
    locked_until TIMESTAMP
);

CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_email ON "user"(email);

-- Role table
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    role_code VARCHAR(50) UNIQUE NOT NULL,
    permissions TEXT,
    is_system BOOLEAN DEFAULT false
);

CREATE INDEX idx_role_code ON role(role_code);

-- User-Role many-to-many relationship
CREATE TABLE user_role (
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- CI Relations table
CREATE TABLE lnk_ci_relation (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    source_ci_id BIGINT NOT NULL REFERENCES configuration_item(id),
    target_ci_id BIGINT NOT NULL REFERENCES configuration_item(id),
    relation_type VARCHAR(50) NOT NULL,
    relation_strength VARCHAR(20) DEFAULT 'medium',
    start_date TIMESTAMP,
    end_date TIMESTAMP
);

CREATE INDEX idx_relation_source ON lnk_ci_relation(source_ci_id);
CREATE INDEX idx_relation_target ON lnk_ci_relation(target_ci_id);
CREATE INDEX idx_relation_type ON lnk_ci_relation(relation_type);

-- Audit log table
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50)
);

CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_time ON audit_log(changed_at);

-- Insert default data
INSERT INTO organization (name, code, type, status) VALUES
('Default Organization', 'DEFAULT', 'company', 'active');

INSERT INTO role (name, role_code, status, is_system, permissions) VALUES
('System Administrator', 'ADMIN', 'active', true, '["*"]'),
('CMDB Administrator', 'CMDB_ADMIN', 'active', true, '["ci:*", "org:*"]'),
('User', 'USER', 'active', true, '["ci:read", "org:read"]');

-- Insert default admin user (password: admin123)
-- BCrypt hash for 'admin123'
INSERT INTO "user" (name, username, email, password, status, auth_method) VALUES
('System Administrator', 'admin', 'admin@itop.local', '$2a$10$CBeKkhOeJUORFZM1GlshZuRfuhK0yaPtkJuLgzKYetS/gv7kvbmCC', 'active', 'LOCAL');

INSERT INTO user_role (user_id, role_id) VALUES (1, 1);

-- Create update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at
CREATE TRIGGER update_organization_updated_at BEFORE UPDATE ON organization
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contact_updated_at BEFORE UPDATE ON contact
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ci_updated_at BEFORE UPDATE ON configuration_item
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_updated_at BEFORE UPDATE ON "user"
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_role_updated_at BEFORE UPDATE ON role
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_relation_updated_at BEFORE UPDATE ON lnk_ci_relation
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
