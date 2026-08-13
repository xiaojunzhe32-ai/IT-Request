-- Generic code table items for configurable workflow dropdown values.

CREATE TABLE IF NOT EXISTS code_table_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'active',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    table_code VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_code_table_item_table_code_code
    ON code_table_item(table_code, code);
CREATE INDEX IF NOT EXISTS idx_code_table_item_table_code ON code_table_item(table_code);
CREATE INDEX IF NOT EXISTS idx_code_table_item_status ON code_table_item(status);
CREATE INDEX IF NOT EXISTS idx_code_table_item_sort ON code_table_item(table_code, sort_order);

DROP TRIGGER IF EXISTS update_code_table_item_updated_at ON code_table_item;

CREATE TRIGGER update_code_table_item_updated_at BEFORE UPDATE ON code_table_item
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

INSERT INTO code_table_item (table_code, code, name, status, description, sort_order)
VALUES
    ('REQUEST_TYPE', 'ACCOUNT_ACCESS', 'Account Access', 'active', 'Account, permission or access request.', 10),
    ('REQUEST_TYPE', 'APPLICATION_ISSUE', 'Application Issue', 'active', 'Application error, defect or unexpected behavior.', 20),
    ('REQUEST_TYPE', 'NETWORK_ISSUE', 'Network Issue', 'active', 'Network connectivity or performance issue.', 30),
    ('REQUEST_TYPE', 'HARDWARE_ISSUE', 'Hardware Issue', 'active', 'Device, workstation or peripheral issue.', 40),
    ('REQUEST_TYPE', 'DATA_CORRECTION', 'Data Correction', 'active', 'Business data correction or cleanup request.', 50),
    ('REQUEST_TYPE', 'OTHER', 'Other', 'active', 'Request type not covered by standard options.', 60),
    ('AFFECTED_SERVICE', 'ERP', 'ERP', 'active', 'Enterprise resource planning system.', 10),
    ('AFFECTED_SERVICE', 'EMAIL', 'Email', 'active', 'Email, mailbox or mailing service.', 20),
    ('AFFECTED_SERVICE', 'NETWORK', 'Network', 'active', 'Network, internet or internal connectivity.', 30),
    ('AFFECTED_SERVICE', 'CRM', 'CRM', 'active', 'Customer relationship management system.', 40),
    ('AFFECTED_SERVICE', 'VPN', 'VPN', 'active', 'Remote access or VPN service.', 50),
    ('AFFECTED_SERVICE', 'PRINTER', 'Printer', 'active', 'Printer or scanning service.', 60),
    ('AFFECTED_SERVICE', 'FINANCE_SYSTEM', 'Finance System', 'active', 'Finance or accounting system.', 70),
    ('AFFECTED_SERVICE', 'HR_SYSTEM', 'HR System', 'active', 'Human resources system.', 80),
    ('AFFECTED_SERVICE', 'OTHER', 'Other', 'active', 'Affected service not covered by standard options.', 90)
ON CONFLICT (table_code, code) DO UPDATE SET
    name = EXCLUDED.name,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;
