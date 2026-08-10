-- Initialize database permissions
-- This script runs after the database is created

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE itop TO itop;

-- Connect to itop database and grant schema permissions
\c itop

GRANT ALL ON SCHEMA public TO itop;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO itop;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO itop;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO itop;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO itop;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";