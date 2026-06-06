-- Run as PostgreSQL superuser (e.g. psql -U postgres -f scripts/setup-local-postgres.sql)
-- Use when Docker is available or local Postgres admin access exists.

CREATE USER talentflow WITH PASSWORD 'talentflow_secret';
CREATE DATABASE talentflow OWNER talentflow;
GRANT ALL PRIVILEGES ON DATABASE talentflow TO talentflow;
