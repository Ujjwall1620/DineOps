-- ============================================================
-- Restaurant Menu Service - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurant_menu_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_menu_db;

-- ─── Menu Items Table ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS menu_items (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(150)    NOT NULL,
    description  TEXT,
    category     VARCHAR(30)     NOT NULL,
    price        DECIMAL(10, 2)  NOT NULL,
    available    BOOLEAN         NOT NULL DEFAULT TRUE,
    image_url    VARCHAR(500),
    created_at   DATETIME(6)     NOT NULL,
    updated_at   DATETIME(6)     NOT NULL,

    CONSTRAINT pk_menu_items   PRIMARY KEY (id),
    CONSTRAINT uq_menu_name    UNIQUE (name),
    CONSTRAINT chk_price       CHECK (price >= 0),
    CONSTRAINT chk_category    CHECK (category IN (
        'STARTER','MAIN_COURSE','DESSERT','BEVERAGE','SIDE_DISH','SPECIAL'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─── Indexes ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_menu_items_category  ON menu_items (category);
CREATE INDEX idx_menu_items_available ON menu_items (available);
CREATE INDEX idx_menu_items_name      ON menu_items (name);
