-- Add SLA deadline columns to ticket table
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS tto_deadline TIMESTAMP;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS ttr_deadline TIMESTAMP;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS sla_id BIGINT;

ALTER TABLE ticket ADD CONSTRAINT fk_ticket_sla FOREIGN KEY (sla_id) REFERENCES sla(id);

CREATE INDEX IF NOT EXISTS idx_ticket_tto_deadline ON ticket(tto_deadline);
CREATE INDEX IF NOT EXISTS idx_ticket_ttr_deadline ON ticket(ttr_deadline);
CREATE INDEX IF NOT EXISTS idx_ticket_sla_id ON ticket(sla_id);

COMMENT ON COLUMN ticket.tto_deadline IS 'Time to Own deadline - when the ticket should be assigned';
COMMENT ON COLUMN ticket.ttr_deadline IS 'Time to Resolve deadline - when the ticket should be resolved';
COMMENT ON COLUMN ticket.sla_id IS 'Reference to the SLA applied to this ticket';