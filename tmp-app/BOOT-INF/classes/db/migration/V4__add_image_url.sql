-- MySQL 5.7 compatibility: older versions reject IF NOT EXISTS on ADD COLUMN
ALTER TABLE products ADD COLUMN image_url VARCHAR(512);
ALTER TABLE order_items ADD COLUMN image_url VARCHAR(512);
