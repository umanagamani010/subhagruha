-- Run this once in MySQL (Workbench or command line)
CREATE DATABASE IF NOT EXISTS shopdb;
USE shopdb;

CREATE TABLE IF NOT EXISTS products (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(255),
    price       DECIMAL(10,2) NOT NULL,
    stock       INT           NOT NULL DEFAULT 0,
    category    VARCHAR(50)   NOT NULL DEFAULT 'General',
    image_file  VARCHAR(100)                 -- file name inside webapp/images, e.g. diya.svg
);

CREATE TABLE IF NOT EXISTS orders (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    customer_name  VARCHAR(100)  NOT NULL,
    phone          VARCHAR(15)   NOT NULL,
    email          VARCHAR(100),
    address        VARCHAR(255)  NOT NULL,
    total          DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20)   NOT NULL,   -- COD or ONLINE
    payment_status VARCHAR(20)   NOT NULL,   -- PENDING, PAID, CANCELLED
    razorpay_order_id   VARCHAR(40),         -- Razorpay order for online payments
    razorpay_payment_id VARCHAR(40),         -- Razorpay payment id after a successful payment
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    order_id   INT           NOT NULL,
    product_id INT           NOT NULL,
    quantity   INT           NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,       -- price at the time of purchase
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Customer reviews (the website also creates this table by itself if it is missing)
CREATE TABLE IF NOT EXISTS reviews (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    product_id    INT          NOT NULL,
    customer_name VARCHAR(60)  NOT NULL,
    rating        TINYINT      NOT NULL,        -- 1 to 5
    review_text   VARCHAR(500) NOT NULL,
    visible       TINYINT(1)   NOT NULL DEFAULT 1,   -- set to 0 to hide a review
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reviews_product (product_id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- SAMPLE products and SAMPLE prices: replace them with your friend's real products, pack sizes and prices
INSERT INTO products (name, description, price, stock, category, image_file) VALUES
('Cow Dung Cakes, pack of 10',  'Sun-dried cow dung cakes for havan and pooja',      120.00, 60, 'Cow dung cakes',  'cowdung.svg'),
('Cow Dung Cakes, pack of 20',  'Sun-dried cow dung cakes for havan and pooja',      220.00, 40, 'Cow dung cakes',  'cowdung.svg'),
('Moringa Leaf Powder, 100 g',  'Dried and ground moringa (drumstick) leaves',       180.00, 30, 'Moringa powder',  'moringa.svg'),
('Moringa Leaf Powder, 250 g',  'Dried and ground moringa (drumstick) leaves',       399.00, 8,  'Moringa powder',  'moringa.svg'),
('Natural Clay Diyas, set of 12','Clay diyas for daily pooja and festivals',          99.00, 80, 'Pooja diyas',     'diya.svg'),
('Natural Clay Diyas, set of 24','Clay diyas for daily pooja and festivals',         180.00, 50, 'Pooja diyas',     'diya.svg');
