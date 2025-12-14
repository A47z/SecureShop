-- Insert test product data for SQL injection prevention demo
-- Using English descriptions to avoid encoding issues

USE secureshop_db;

-- Clear existing products
DELETE FROM products;

-- Insert test products
INSERT INTO products (name, description, price, stock, image_url, category, active, created_at, updated_at)
VALUES
('iPhone 15 Pro', 'Latest flagship phone, A17 Pro chip, titanium design', 7999.00, 50, '/images/iphone15.jpg', 'Phone', 1, NOW(), NOW()),
('MacBook Pro 16', 'Professional laptop, M3 Max chip, 64GB RAM', 25999.00, 20, '/images/macbook.jpg', 'Computer', 1, NOW(), NOW()),
('AirPods Pro 2', 'Active noise cancelling wireless earbuds', 1899.00, 100, '/images/airpods.jpg', 'Accessory', 1, NOW(), NOW()),
('iPad Air', 'Lightweight tablet, M1 chip, 10.9 inch display', 4799.00, 30, '/images/ipad.jpg', 'Tablet', 1, NOW(), NOW()),
('Apple Watch S9', 'Smart watch, S9 chip, health monitoring', 3199.00, 60, '/images/watch.jpg', 'Wearable', 1, NOW(), NOW()),
('Galaxy S24', 'Samsung flagship, Snapdragon 8 Gen 3', 6999.00, 40, '/images/galaxy.jpg', 'Phone', 1, NOW(), NOW()),
('Xiaomi 14 Pro', 'Leica camera, Snapdragon 8 Gen 3', 4999.00, 80, '/images/xiaomi14.jpg', 'Phone', 1, NOW(), NOW()),
('Mate 60 Pro', 'Satellite communication, Kirin 9000S', 6499.00, 35, '/images/huawei.jpg', 'Phone', 1, NOW(), NOW()),
('ThinkPad X1', 'Business laptop, Intel Core i7, carbon fiber', 12999.00, 15, '/images/thinkpad.jpg', 'Computer', 1, NOW(), NOW()),
('Sony WH-1000XM5', 'Premium noise cancelling headphones', 2699.00, 45, '/images/sony.jpg', 'Accessory', 1, NOW(), NOW());

-- Verify inserted data
SELECT id, name, price, category, active FROM products;

-- Success message
SELECT 'Test product data inserted successfully!' AS message;
