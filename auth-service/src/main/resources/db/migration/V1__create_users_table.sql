CREATE SCHEMA IF NOT EXISTS authdb;

CREATE TABLE authdb.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    full_name     VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);
