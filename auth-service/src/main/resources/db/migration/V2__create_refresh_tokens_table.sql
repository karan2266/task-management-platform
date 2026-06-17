CREATE TABLE authdb.refresh_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES authdb.users(id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE
);
