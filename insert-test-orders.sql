-- Insert test orders for IDOR demonstration
-- Creates test users and their orders

USE secureshop_db;

-- Create test users
-- User 1: alice (password: Alice@2024!Test)
DELETE FROM users WHERE username = 'alice';
INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
VALUES (
    'alice',
    'alice@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8pJnF8PxjZ8b0K0Xp3qX.H0ZK.6h.',
    'USER',
    1,
    NOW(),
    NOW()
);

-- User 2: bob (password: Bob@2024!Test)
DELETE FROM users WHERE username = 'bob';
INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
VALUES (
    'bob',
    'bob@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8pJnF8PxjZ8b0K0Xp3qX.H0ZK.6h.',
    'USER',
    1,
    NOW(),
    NOW()
);

-- Get user IDs
SET @alice_id = (SELECT id FROM users WHERE username = 'alice');
SET @bob_id = (SELECT id FROM users WHERE username = 'bob');

-- Clear existing orders for test users
DELETE FROM order_items WHERE order_id IN (
    SELECT id FROM orders WHERE user_id IN (@alice_id, @bob_id)
);
DELETE FROM orders WHERE user_id IN (@alice_id, @bob_id);

-- Create orders for Alice (user 1)
INSERT INTO orders (user_id, status, total_amount, shipping_address, receiver_name, receiver_phone, created_at)
VALUES 
    (@alice_id, 'PAID', 9898.00, '123 Main St, New York, NY 10001', 'Alice Smith', '555-0101', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (@alice_id, 'SHIPPED', 1899.00, '123 Main St, New York, NY 10001', 'Alice Smith', '555-0101', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (@alice_id, 'PENDING', 4799.00, '123 Main St, New York, NY 10001', 'Alice Smith', '555-0101', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Get Alice's order IDs
SET @alice_order1 = LAST_INSERT_ID();
SET @alice_order2 = @alice_order1 + 1;
SET @alice_order3 = @alice_order1 + 2;

-- Create order items for Alice's orders
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, product_name_at_purchase)
VALUES 
    -- Order 1: iPhone + AirPods
    (@alice_order1, 11, 1, 7999.00, 'iPhone 15 Pro'),
    (@alice_order1, 13, 1, 1899.00, 'AirPods Pro 2'),
    -- Order 2: AirPods only
    (@alice_order2, 13, 1, 1899.00, 'AirPods Pro 2'),
    -- Order 3: iPad
    (@alice_order3, 14, 1, 4799.00, 'iPad Air');

-- Create orders for Bob (user 2)
INSERT INTO orders (user_id, status, total_amount, shipping_address, receiver_name, receiver_phone, created_at)
VALUES 
    (@bob_id, 'COMPLETED', 25999.00, '456 Oak Ave, Los Angeles, CA 90001', 'Bob Johnson', '555-0202', DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (@bob_id, 'PAID', 10198.00, '456 Oak Ave, Los Angeles, CA 90001', 'Bob Johnson', '555-0202', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Get Bob's order IDs
SET @bob_order1 = LAST_INSERT_ID();
SET @bob_order2 = @bob_order1 + 1;

-- Create order items for Bob's orders
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, product_name_at_purchase)
VALUES 
    -- Order 1: MacBook
    (@bob_order1, 12, 1, 25999.00, 'MacBook Pro 16'),
    -- Order 2: Galaxy + Watch
    (@bob_order2, 16, 1, 6999.00, 'Galaxy S24'),
    (@bob_order2, 15, 1, 3199.00, 'Apple Watch S9');

-- Display test accounts and orders
SELECT '=== Test Accounts Created ===' AS info;
SELECT id, username, email, role FROM users WHERE username IN ('alice', 'bob');


SELECT o.id, o.status, o.total_amount, o.receiver_name, o.created_at
FROM orders o
WHERE o.user_id = @alice_id
ORDER BY o.created_at DESC;


SELECT o.id, o.status, o.total_amount, o.receiver_name, o.created_at
FROM orders o
WHERE o.user_id = @bob_id
ORDER BY o.created_at DESC;


SELECT 'Login as alice (password: Alice@2024!Test)' AS step1;
SELECT 'View your orders and note the order IDs' AS step2;
SELECT 'Logout and login as bob (password: Bob@2024!Test)' AS step3;
SELECT 'Try to access Alice order URL: /orders/{alice_order_id}' AS step4;
SELECT 'Expected: 403 Forbidden - Cannot access other user orders!' AS step5;
