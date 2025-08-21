-- liquibase formatted sql

-- changeset pavel11sg:1.1-create_items_table
CREATE TABLE items
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) UNIQUE            NOT NULL,
    price          DECIMAL(10, 2)                 NOT NULL CHECK (price >= 0),
    description    TEXT,
    stock_quantity INTEGER          DEFAULT 0     NOT NULL CHECK (stock_quantity >= 0),
    created_at     TIMESTAMP        DEFAULT NOW() NOT NULL,
    updated_at     TIMESTAMP        DEFAULT NOW() NOT NULL
);
-- rollback DROP TABLE items;

-- changeset pavel11sg:1.2-create_orders_table
CREATE TABLE orders
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    user_id       UUID           NOT NULL,
    status        VARCHAR(50)    NOT NULL DEFAULT 'CREATED',
    total_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    creation_date TIMESTAMP               DEFAULT NOW() NOT NULL,
    updated_at    TIMESTAMP               DEFAULT NOW() NOT NULL
);
-- rollback DROP TABLE orders;

-- changeset pavel11sg:1.3-create_order_items_table
CREATE TABLE order_items
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID                           NOT NULL,
    item_id    UUID                           NOT NULL,
    quantity   INTEGER                        NOT NULL CHECK (quantity > 0),
    price      DECIMAL(10, 2)                 NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP        DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT uq_order_item UNIQUE (order_id, item_id)
);
-- rollback DROP TABLE order_items;

-- changeset pavel11sg:1.4-create_indexes
CREATE INDEX idx_items_name ON items (name);
CREATE INDEX idx_items_price ON items (price);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_creation_date ON orders (creation_date);
CREATE INDEX idx_orders_user_status ON orders (user_id, status);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_item_id ON order_items (item_id);
CREATE INDEX idx_order_items_order_item ON order_items (order_id, item_id);
-- rollback DROP INDEX idx_items_name;
-- rollback DROP INDEX idx_items_price;
-- rollback DROP INDEX idx_orders_user_id;
-- rollback DROP INDEX idx_orders_status;
-- rollback DROP INDEX idx_orders_creation_date;
-- rollback DROP INDEX idx_orders_user_status;
-- rollback DROP INDEX idx_order_items_order_id;
-- rollback DROP INDEX idx_order_items_item_id;
-- rollback DROP INDEX idx_order_items_order_item;