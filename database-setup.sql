# ========================================
# SecureShop 数据库创建和用户配置脚本
# 
# 对应 OWASP 实验步骤5: 基本的安全配置指南
# 对应 OWASP 实验步骤1: 使用低权限用户
# ========================================

# 使用方法:
# 1. 以root用户登录MySQL: mysql -u root -p
# 2. 执行此脚本: source database-setup.sql
# 或者直接: mysql -u root -p < database-setup.sql

# ========================================
# 创建数据库
# ========================================

CREATE DATABASE IF NOT EXISTS secureshop_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

# ========================================
# 创建专用数据库用户（低权限用户）
# 
# 实验步骤5: 使用低权限的系统用户来运行数据库
# 实验步骤5: 确保输入用于连接数据库服务器的用户不是数据库管理员
# ========================================

# 删除已存在的用户（如果存在）
DROP USER IF EXISTS 'secureshop_user'@'localhost';

# 创建新用户（使用强密码）
CREATE USER 'secureshop_user'@'localhost' 
    IDENTIFIED BY 'SecureShop@2024!Strong';

# ========================================
# 授予最小必要权限
# 
# 只授予应用运行所需的最小权限:
# - SELECT: 查询数据
# - INSERT: 插入数据
# - UPDATE: 更新数据
# - DELETE: 删除数据
# 
# 不授予的权限:
# - DROP: 删除表
# - CREATE: 创建表 (由Hibernate DDL自动处理，生产环境应禁用)
# - ALTER: 修改表结构
# - GRANT: 授权给其他用户
# - SUPER: 超级管理员权限
# ========================================

GRANT SELECT, INSERT, UPDATE, DELETE 
    ON secureshop_db.* 
    TO 'secureshop_user'@'localhost';

# 如果使用Hibernate DDL auto-update，需要额外授予CREATE和ALTER权限
# 生产环境应该移除这些权限，使用 ddl-auto=validate
GRANT CREATE, ALTER 
    ON secureshop_db.* 
    TO 'secureshop_user'@'localhost';

# 刷新权限
FLUSH PRIVILEGES;

# ========================================
# 验证用户权限
# ========================================

SHOW GRANTS FOR 'secureshop_user'@'localhost';

# ========================================
# 安全建议
# ========================================
# 
# 1. 定期更换数据库密码
# 2. 使用强密码策略（至少12字符，包含大小写字母、数字、特殊字符）
# 3. 生产环境禁用Hibernate DDL自动更新，使用Flyway或Liquibase进行数据库迁移
# 4. 启用MySQL审计日志，记录所有数据库操作
# 5. 定期备份数据库
# 6. 限制数据库只能从应用服务器IP访问，禁止外部访问
# 7. 启用SSL加密数据库连接
# 
# SSL配置示例（在application.properties中）:
# spring.datasource.url=jdbc:mysql://localhost:3306/secureshop_db?useSSL=true&requireSSL=true
#
# ========================================
