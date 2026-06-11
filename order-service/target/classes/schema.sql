-- ============================================================
-- Restaurant Order Service - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurant_order_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_order_db;

-- ─── Orders Table ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    order_number  VARCHAR(50)     NOT NULL,
    table_number  INT             NOT NULL,
    waiter_id     BIGINT          NOT NULL,
    waiter_name   VARCHAR(100)    NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    total_amount  DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    created_at    DATETIME(6)     NOT NULL,
    updated_at    DATETIME(6)     NOT NULL,

    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uq_order_number UNIQUE (order_number),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING','PREPARING','READY','SERVED','CANCELLED')),
    CONSTRAINT chk_table_number CHECK (table_number > 0),
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Order Items Table ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    order_id        BIGINT          NOT NULL,
    menu_item_id    BIGINT          NOT NULL,
    menu_item_name  VARCHAR(150)    NOT NULL,
    quantity        INT             NOT NULL,
    price_per_unit  DECIMAL(10, 2)  NOT NULL,
    subtotal        DECIMAL(10, 2)  NOT NULL,

    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_price_per_unit CHECK (price_per_unit >= 0),
    CONSTRAINT chk_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Indexes ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_orders_status       ON orders (status);
CREATE INDEX idx_orders_table        ON orders (table_number);
CREATE INDEX idx_orders_waiter       ON orders (waiter_id);
CREATE INDEX idx_orders_created_at   ON orders (created_at);
CREATE INDEX idx_order_items_order   ON order_items (order_id);
CREATE INDEX idx_order_items_menu    ON order_items (menu_item_id);
