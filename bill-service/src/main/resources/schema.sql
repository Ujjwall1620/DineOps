-- ============================================================
-- Restaurant Bill Service - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurant_bill_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_bill_db;

-- ─── Bills ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    bill_number          VARCHAR(50)    NOT NULL,
    order_id             BIGINT         NOT NULL,
    order_number         VARCHAR(50)    NOT NULL,
    table_number         INT            NOT NULL,
    waiter_id            BIGINT         NOT NULL,
    waiter_name          VARCHAR(100)   NOT NULL,
    subtotal             DECIMAL(10,2)  NOT NULL,
    gst_percentage       DECIMAL(5,2)   NOT NULL,
    gst_amount           DECIMAL(10,2)  NOT NULL,
    service_charge_pct   DECIMAL(5,2)   NOT NULL DEFAULT 0.00,
    service_charge_amt   DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    grand_total          DECIMAL(10,2)  NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'GENERATED',
    payment_method       VARCHAR(30),
    idempotency_key      VARCHAR(100),
    payment_url          VARCHAR(500),
    version              BIGINT         NOT NULL DEFAULT 0,
    created_at           DATETIME(6)    NOT NULL,
    updated_at           DATETIME(6)    NOT NULL,

    CONSTRAINT pk_bills            PRIMARY KEY (id),
    CONSTRAINT uq_bill_number      UNIQUE (bill_number),
    CONSTRAINT uq_order_id         UNIQUE (order_id),
    CONSTRAINT uq_idempotency_key  UNIQUE (idempotency_key),
    CONSTRAINT chk_bill_status     CHECK (status IN (
        'PENDING','GENERATED','PAYMENT_PENDING','PAID','FAILED','CANCELLED','REFUNDED'
    )),
    CONSTRAINT chk_grand_total     CHECK (grand_total >= 0),
    CONSTRAINT chk_subtotal        CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Bill Items ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bill_items (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    bill_id         BIGINT         NOT NULL,
    menu_item_id    BIGINT         NOT NULL,
    menu_item_name  VARCHAR(150)   NOT NULL,
    quantity        INT            NOT NULL,
    price_per_unit  DECIMAL(10,2)  NOT NULL,
    subtotal        DECIMAL(10,2)  NOT NULL,

    CONSTRAINT pk_bill_items         PRIMARY KEY (id),
    CONSTRAINT fk_bill_items_bill    FOREIGN KEY (bill_id)
        REFERENCES bills(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_item_quantity     CHECK (quantity > 0),
    CONSTRAINT chk_item_price        CHECK (price_per_unit >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Payment Transactions ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payment_transactions (
    id                       BIGINT         NOT NULL AUTO_INCREMENT,
    transaction_ref          VARCHAR(100)   NOT NULL,
    bill_id                  BIGINT         NOT NULL,
    amount                   DECIMAL(10,2)  NOT NULL,
    payment_method           VARCHAR(30)    NOT NULL,
    gateway                  VARCHAR(30)    NOT NULL,
    gateway_transaction_id   VARCHAR(200),
    gateway_response         TEXT,
    status                   VARCHAR(20)    NOT NULL,
    failure_reason           VARCHAR(500),
    created_at               DATETIME(6)    NOT NULL,

    CONSTRAINT pk_payment_transactions   PRIMARY KEY (id),
    CONSTRAINT uq_transaction_ref        UNIQUE (transaction_ref),
    CONSTRAINT chk_txn_status            CHECK (status IN (
        'INITIATED','SUCCESS','FAILED','REFUNDED'
    )),
    CONSTRAINT chk_txn_amount            CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Indexes ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_bills_status          ON bills (status);
CREATE INDEX idx_bills_order_id        ON bills (order_id);
CREATE INDEX idx_bills_waiter_id       ON bills (waiter_id);
CREATE INDEX idx_bills_created_at      ON bills (created_at);
CREATE INDEX idx_bill_items_bill       ON bill_items (bill_id);
CREATE INDEX idx_bill_items_menu       ON bill_items (menu_item_id);
CREATE INDEX idx_txn_bill_id           ON payment_transactions (bill_id);
CREATE INDEX idx_txn_status            ON payment_transactions (status);
CREATE INDEX idx_txn_gateway_id        ON payment_transactions (gateway_transaction_id);
