-- Replace Affected Services / Systems dropdown values with ECCMS/ECStation/REPORT options.

DELETE FROM code_table_item
WHERE table_code = 'AFFECTED_SERVICE';

INSERT INTO code_table_item (table_code, code, name, status, description, sort_order)
VALUES
    ('AFFECTED_SERVICE', 'ECCMS_FATAL_LETTER', 'ECCMS-FATAL-LETTER', 'active', NULL, 10),
    ('AFFECTED_SERVICE', 'ECCMS_NON_FATAL', 'ECCMS-NON-FATAL', 'active', NULL, 20),
    ('AFFECTED_SERVICE', 'ECCMS_NON_FATAL_LET', 'ECCMS-NON-FATAL-LET.', 'active', NULL, 30),
    ('AFFECTED_SERVICE', 'ECCMS_OMU', 'ECCMS-OMU', 'active', NULL, 40),
    ('AFFECTED_SERVICE', 'ECSTATION', 'ECStation', 'active', NULL, 50),
    ('AFFECTED_SERVICE', 'ECSTATION_CHATBOT', 'ECStation-CHATBOT', 'active', NULL, 60),
    ('AFFECTED_SERVICE', 'ECSTATION_FATAL', 'ECStation-FATAL', 'active', NULL, 70),
    ('AFFECTED_SERVICE', 'ECSTATION_NON_FATAL', 'ECStation-NON-FATAL', 'active', NULL, 80),
    ('AFFECTED_SERVICE', 'ECSTATION_PMCOS', 'ECStation-PMCOS', 'active', NULL, 90),
    ('AFFECTED_SERVICE', 'ECSTATION_STATIC', 'ECStation-STATIC', 'active', NULL, 100),
    ('AFFECTED_SERVICE', 'IVRS', 'IVRS', 'active', NULL, 110),
    ('AFFECTED_SERVICE', 'OTHERS', 'Others', 'active', NULL, 120),
    ('AFFECTED_SERVICE', 'PMCOS', 'PMCOS', 'active', NULL, 130),
    ('AFFECTED_SERVICE', 'REPORT', 'REPORT', 'active', NULL, 140),
    ('AFFECTED_SERVICE', 'REPORT_AAID', 'REPORT-AAID', 'active', NULL, 150),
    ('AFFECTED_SERVICE', 'REPORT_FATAL', 'REPORT-FATAL', 'active', NULL, 160),
    ('AFFECTED_SERVICE', 'REPORT_FCO', 'REPORT-FCO', 'active', NULL, 170),
    ('AFFECTED_SERVICE', 'REPORT_NON_FATALI', 'REPORT-NON-FATALI', 'active', NULL, 180),
    ('AFFECTED_SERVICE', 'REPORT_OP1', 'REPORT-OP1', 'active', NULL, 190),
    ('AFFECTED_SERVICE', 'REPORT_OP2', 'REPORT-OP2', 'active', NULL, 200),
    ('AFFECTED_SERVICE', 'REPORT_PMCOS', 'REPORT-PMCOS', 'active', NULL, 210);
