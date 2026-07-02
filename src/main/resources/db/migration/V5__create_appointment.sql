CREATE TABLE appointment (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id        UUID           NOT NULL REFERENCES branch(id),
    customer_id      UUID           NOT NULL REFERENCES customer(id),
    stylist_id       UUID           NOT NULL REFERENCES stylist(id),
    combo_id         UUID           REFERENCES combo(id),
    status           VARCHAR(20)    NOT NULL,
    scheduled_date   DATE           NOT NULL,
    start_time       TIME           NOT NULL,
    end_time         TIME           NOT NULL,
    total_price      DECIMAL(10,2)  NOT NULL,
    rejection_reason TEXT,
    expires_at       TIMESTAMPTZ    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ
);

CREATE TABLE appointment_service (
    id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id  UUID           NOT NULL REFERENCES appointment(id),
    service_id      UUID           NOT NULL REFERENCES service(id),
    price_at_booking DECIMAL(10,2) NOT NULL,
    duration        INTEGER        NOT NULL
);

CREATE INDEX idx_appointment_branch_date_status ON appointment(branch_id, scheduled_date, status);
CREATE INDEX idx_appointment_stylist_date       ON appointment(stylist_id, scheduled_date);
CREATE INDEX idx_appointment_customer           ON appointment(customer_id);
CREATE INDEX idx_appointment_status_expires     ON appointment(status, expires_at);
