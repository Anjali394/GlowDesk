CREATE TABLE service (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id   UUID           NOT NULL REFERENCES branch(id),
    name        VARCHAR(100)   NOT NULL,
    description TEXT,
    duration    INTEGER        NOT NULL,
    price       DECIMAL(10,2)  NOT NULL,
    is_active   BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE INDEX idx_service_branch_active ON service(branch_id, is_active);

CREATE TABLE combo (
    id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id      UUID           NOT NULL REFERENCES branch(id),
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    discount_type  VARCHAR(20)    NOT NULL,
    discount_value DECIMAL(10,2)  NOT NULL,
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100)
);

CREATE TABLE combo_service (
    combo_id   UUID NOT NULL REFERENCES combo(id),
    service_id UUID NOT NULL REFERENCES service(id),
    PRIMARY KEY (combo_id, service_id)
);
