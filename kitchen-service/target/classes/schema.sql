-- ============================================================
-- Restaurant Kitchen Service - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurant_kitchen_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_kitchen_db;

-- ─── Kitchen Tickets ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS kitchen_tickets (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    order_id      BIGINT       NOT NULL,
    order_number  VARCHAR(50)  NOT NULL,
    table_number  INT          NOT NULL,
    chef_id       BIGINT,
    chef_name     VARCHAR(100),
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,

    CONSTRAINT pk_kitchen_tickets    PRIMARY KEY (id),
    CONSTRAINT uq_order_id           UNIQUE (order_id),
    CONSTRAINT chk_kitchen_status    CHECK (status IN (
        'PENDING','IN_PREPARATION','READY','COMPLETED','CANCELLED'
    )),
    CONSTRAINT chk_table_number      CHECK (table_number > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Kitchen Items ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS kitchen_items (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    kitchen_ticket_id BIGINT       NOT NULL,
    menu_item_id      BIGINT       NOT NULL,
    menu_item_name    VARCHAR(150) NOT NULL,
    quantity          INT          NOT NULL,

    CONSTRAINT pk_kitchen_items         PRIMARY KEY (id),
    CONSTRAINT fk_kitchen_items_ticket  FOREIGN KEY (kitchen_ticket_id)
        REFERENCES kitchen_tickets(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_item_quantity        CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Indexes ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_kt_status      ON kitchen_tickets (status);
CREATE INDEX idx_kt_order_id    ON kitchen_tickets (order_id);
CREATE INDEX idx_kt_created_at  ON kitchen_tickets (created_at);
CREATE INDEX idx_kt_chef_id     ON kitchen_tickets (chef_id);
CREATE INDEX idx_ki_ticket      ON kitchen_items (kitchen_ticket_id);
CREATE INDEX idx_ki_menu_item   ON kitchen_items (menu_item_id);
