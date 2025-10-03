-- liquibase formatted sql

-- changeset pavel11sg:2.1-add-currency-payment-columns-to-orders

ALTER TABLE orders
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    ADD COLUMN payment_method_token VARCHAR(255);

-- rollback ALTER TABLE orders DROP COLUMN currency;
-- rollback ALTER TABLE orders DROP COLUMN payment_method_token;