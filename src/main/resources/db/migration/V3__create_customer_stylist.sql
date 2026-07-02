CREATE TABLE customer (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL UNIQUE REFERENCES "user"(id),
    first_name VARCHAR(80) NOT NULL,
    last_name  VARCHAR(80),
    phone      VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE stylist (
    id         UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID           NOT NULL UNIQUE REFERENCES "user"(id),
    branch_id  UUID           NOT NULL REFERENCES branch(id),
    first_name VARCHAR(80)    NOT NULL,
    last_name  VARCHAR(80),
    experience INTEGER        NOT NULL,
    rating     DECIMAL(3,2)   NOT NULL DEFAULT 0.00,
    is_active  BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_stylist_branch_active ON stylist(branch_id, is_active);
