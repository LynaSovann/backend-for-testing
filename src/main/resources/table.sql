CREATE TABLE products
(
    product_id   SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    image_url    TEXT,
    description  TEXT
);