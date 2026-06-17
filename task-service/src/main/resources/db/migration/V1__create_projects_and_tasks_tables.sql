CREATE SCHEMA IF NOT EXISTS taskdb;

CREATE TABLE taskdb.projects (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at    TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW(),
    description   TEXT,
    name          VARCHAR(255) NOT NULL,
    owner_user_id UUID         NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE taskdb.tasks (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    assignee_user_id UUID,
    created_at       TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW(),
    description      TEXT,
    priority         VARCHAR(255) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    status           VARCHAR(255) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    title            VARCHAR(255) NOT NULL,
    updated_at       TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW(),
    project_id       UUID         NOT NULL REFERENCES taskdb.projects(id) ON DELETE CASCADE
);
