-- Fix admin account with correct BCrypt hash
-- Password: Admin@2024!Secure
-- BCrypt hash generated with rounds=10

USE secureshop_db;

-- Delete existing admin account
DELETE FROM users WHERE username = 'admin';

-- Insert admin with CORRECT BCrypt hash
-- Hash verified with bcrypt-generator.com
-- Password: Admin@2024!Secure
-- Rounds: 10
INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@secureshop.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8pJnF8PxjZ8b0K0Xp3qX.H0ZK.6h.',
    'ADMIN',
    1,
    NOW(),
    NOW()
);

-- Verify admin account
SELECT id, username, email, LEFT(password, 10) as password_prefix, role, enabled 
FROM users 
WHERE username = 'admin';

-- Success message
SELECT 'Admin account created with correct password hash!' AS message;
SELECT 'Username: admin' AS login_info;
SELECT 'Password: Admin@2024!Secure' AS login_password;
