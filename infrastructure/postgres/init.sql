-- Create the reviq database if it doesn't exist
SELECT 'CREATE DATABASE reviq_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'reviq_db');

-- Connect to the database and enable extensions
\c reviq_db;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
