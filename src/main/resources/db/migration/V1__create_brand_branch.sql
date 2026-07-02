CREATE TABLE brand (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE TABLE branch (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    brand_id     UUID        NOT NULL REFERENCES brand(id),
    name         VARCHAR(100) NOT NULL,
    address      TEXT,
    phone        VARCHAR(20),
    opening_time TIME        NOT NULL,
    closing_time TIME        NOT NULL,
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100)
);
