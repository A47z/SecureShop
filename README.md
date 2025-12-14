# 🛡️ SecureShop - 电商安全演示系统

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![OWASP](https://img.shields.io/badge/OWASP-Top%2010-red.svg)](https://owasp.org/Top10/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

一个严格遵循 **OWASP Top 10** 安全标准的电商系统演示项目，展示了如何在实际应用中实现企业级Web安全防护措施。

## � 项目简介

SecureShop 是一个教育性质的电商安全演示系统，旨在帮助开发者理解和实践Web应用安全最佳实践。项目完整实现了OWASP Top 10中的10项安全防护措施，每个功能都包含详细的代码注释和安全说明。

**适用场景：**

- 🎓 学习Web安全最佳实践
- 💼 企业安全培训演示
- 📚 毕业设计/课程项目
- 🔍 安全代码审查参考

## ✨ 核心特性

### 🔐 完整的OWASP Top 10安全实现

| # | 安全措施 | 实现状态 | 演示功能 |
|---|---------|---------|---------|
| 1 | SQL注入预防 | ✅ 100% | 商品搜索 |
| 2 | 身份验证和会话管理 | ✅ 100% | 用户注册/登录 |
| 3 | XSS防护 | ✅ 100% | 留言板 |
| 4 | IDOR防护 | ✅ 100% | 订单管理 |
| 5 | 安全配置 | ✅ 100% | 错误页面/响应头 |
| 6 | 敏感数据保护 | ⚠️ 50% | 密码加密 |
| 7 | 访问控制 (RBAC) | ✅ 100% | 管理员功能 |
| 8 | CSRF防护 | ✅ 100% | 所有表单 |
| 9 | 依赖安全审计 | ⚠️ 30% | Maven依赖管理 |
| 10 | 重定向验证 | ✅ 100% | URL白名单 |

### 🎯 主要功能

- **用户管理**：注册、登录、强密码验证、BCrypt加密
- **商品浏览**：商品列表、搜索（SQL注入防护演示）
- **订单系统**：创建订单、查看订单（IDOR防护演示）
- **评论功能**：发表评论（XSS防护演示）
- **管理后台**：用户管理、订单管理（RBAC演示）
- **安全演示**：每个功能都包含测试用例和攻击场景演示

## 🛠️ 技术栈

**后端框架：**
- Java 17
- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- Hibernate

**前端技术：**
- Thymeleaf 模板引擎
- HTML5 / CSS3
- 响应式设计

**数据库：**
- MySQL 8.0

**构建工具：**

- Maven 3.x

## 🚀 快速开始

### 📋 环境要求

- ✅ Java 17 或更高版本
- ✅ MySQL 8.0 或更高版本
- ✅ Maven 3.6 或更高版本

### 📦 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/A47z/SecureShop.git
cd SecureShop
```

#### 2. 配置数据库

**创建数据库和用户：**

```bash
# 使用root用户登录MySQL
mysql -u root -p

# 执行初始化脚本
source database-setup.sql
```

或者手动执行：

```sql
CREATE DATABASE IF NOT EXISTS secureshop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'secureshop_user'@'localhost' IDENTIFIED BY 'SecureShop@2024!Strong';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER ON secureshop_db.* TO 'secureshop_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. 创建管理员账户

```bash
mysql -u root -p < create-admin.sql
```

**默认管理员账户：**

- 用户名: `admin`
- 密码: `Admin@2024!Secure`

#### 4. 插入测试数据

```bash
# 插入测试商品
mysql -u root -p < insert-test-data.sql

# 插入测试订单（用于IDOR演示）
mysql -u root -p < insert-test-orders.sql
```

这将创建：
- 10个测试商品（iPhone、MacBook等）
- 2个测试用户账户（alice、bob）
- 5个测试订单

#### 5. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

#### 6. 访问系统

打开浏览器访问：`http://localhost:8080`

## 📚 使用教程

### 🔑 用户登录

**管理员账户：**

```
用户名: admin
密码: Admin@2024!Secure
```

**测试用户账户：**

```
用户 alice:
  用户名: alice
  密码: Alice@2024!Test

用户 bob:
  用户名: bob
  密码: Bob@2024!Test
```

### 🎯 安全功能演示

#### 1️⃣ SQL注入防护演示

**测试步骤：**
1. 登录系统
2. 点击主页的 "SQL 注入预防" 卡片
3. 在搜索框输入恶意SQL：`test' OR '1'='1`
4. 点击搜索

**预期结果：**
- ✅ 系统将其作为普通字符串搜索
- ✅ 不会返回所有商品
- ✅ 防护成功！

**测试用例：**
```
❌ test' OR '1'='1
❌ '; DROP TABLE products; --
❌ ' UNION SELECT * FROM users --
```

---

#### 2️⃣ 强密码策略演示

**测试步骤：**
1. 访问注册页面：`http://localhost:8080/register`
2. 尝试各种密码强度

**测试用例：**
```
❌ password123          (缺少大写和特殊字符)
❌ Pass@123             (少于10字符)
❌ Password123          (缺少特殊字符)
✅ MyP@ssw0rd2024!      (符合要求)
```

**密码要求：**
- 至少10个字符
- 包含大写字母
- 包含小写字母
- 包含数字
- 包含特殊字符

---

#### 3️⃣ XSS防护演示

**测试步骤：**
1. 登录系统
2. 点击 "XSS 防护" 进入留言板
3. 尝试输入恶意脚本：`<script>alert('XSS')</script>`
4. 提交评论

**预期结果：**
- ✅ 评论显示为纯文本
- ✅ 脚本不会执行
- ✅ HTML被自动转义

**测试用例：**
```html
❌ <script>alert('XSS')</script>
❌ <img src=x onerror=alert('XSS')>
❌ <svg onload=alert('XSS')>
```

---

#### 4️⃣ IDOR防护演示

**测试步骤：**

1. **准备阶段：**
   - 以 alice 登录（alice / Alice@2024!Test）
   - 点击 "创建测试订单" 按钮创建几个订单
   - 点击查看订单详情，记住URL（例如：`/orders/9`）
   - 登出

2. **攻击测试：**
   - 以 bob 登录（bob / Bob@2024!Test）
   - 手动访问 alice 的订单URL：`http://localhost:8080/orders/9`

**预期结果：**
- ✅ 返回 403 Forbidden 错误页面
- ✅ 显示 "订单不存在或无权访问"
- ✅ Bob 无法查看 Alice 的订单
- ✅ IDOR 防护成功！

---

#### 5️⃣ CSRF防护演示

**测试步骤：**
1. 访问任意表单页面（如登录页）
2. 右键 → "查看网页源代码"
3. 搜索 `_csrf`（Ctrl+F）

**预期结果：**
```html
✅ 找到隐藏的CSRF Token：
<input type="hidden" name="_csrf" value="a1b2c3d4-..."/>
```

**或者使用开发者工具：**
1. 按 F12 打开开发者工具
2. 切换到 "Network" 标签
3. 提交表单
4. 查看请求的 Form Data

**预期看到：**
```
Form Data:
  username: admin
  password: ********
  _csrf: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

#### 6️⃣ 访问控制（RBAC）演示

**测试步骤：**
1. 以普通用户登录（alice 或 bob）
2. 尝试访问管理员页面：`http://localhost:8080/admin/users`

**预期结果：**
- ✅ 返回 403 Forbidden
- ✅ 只有ADMIN角色可以访问

**测试管理员权限：**
1. 以 admin 登录
2. 访问 `/admin/users` → ✅ 允许访问

---

#### 7️⃣ 重定向验证演示

**测试步骤：**
1. 访问重定向演示页面：`http://localhost:8080/redirect/demo`
2. 点击测试用例

**安全重定向（允许）：**
```
✅ /redirect/safe?url=/home
✅ /redirect/safe?url=http://localhost:8080/home
```

**恶意重定向（拒绝）：**
```
❌ /redirect/safe?url=https://evil.com
❌ /redirect/safe?url=javascript:alert('XSS')
```

**预期结果：**
- ✅ 外部URL被阻止
- ✅ 显示 "不允许重定向到外部网站"

---

#### 8️⃣ 安全错误处理演示

**测试步骤：**
1. 访问不存在的订单：`http://localhost:8080/orders/99999`

**预期结果：**
- ✅ 显示友好的500错误页面
- ✅ 显示唯一错误ID（如：`错误ID: c15dd4ec-...`）
- ❌ **不显示**堆栈跟踪
- ❌ **不显示**文件路径
- ❌ **不显示**SQL查询语句



## 🧪 测试指南

### 单元测试

```bash
mvn test
```

### 构建生产包

```bash
mvn clean package -DskipTests
java -jar target/secureshop-0.0.1-SNAPSHOT.jar
```



## 📜 开源协议

本项目采用 [MIT License](LICENSE)

