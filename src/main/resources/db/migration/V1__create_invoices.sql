CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE invoices (
    id            BIGINT PRIMARY KEY,
    customer_id   VARCHAR(64)    NOT NULL,
    currency      CHAR(3)        NOT NULL,
    status        VARCHAR(16)    NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    payment_token VARCHAR(64)    NOT NULL UNIQUE,
    issued_at     TIMESTAMPTZ    NOT NULL,
    paid_at       TIMESTAMPTZ
);

CREATE TABLE invoice_lines (
    invoice_id  BIGINT         NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    line_no     INT            NOT NULL,
    description VARCHAR(255)   NOT NULL,
    quantity    INT            NOT NULL,
    unit_price  NUMERIC(12, 2) NOT NULL,
    PRIMARY KEY (invoice_id, line_no)
);
