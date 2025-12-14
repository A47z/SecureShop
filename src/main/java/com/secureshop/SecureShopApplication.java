package com.secureshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SecureShop 电商系统主启动类
 * 
 * 基于 OWASP Top 10 安全要求构建的安全电商演示系统
 * 
 * 实现的安全特性:
 * - 实验步骤1: SQL 注入预防 (JPA 参数化查询)
 * - 实验步骤2: 身份验证和会话管理 (Spring Security + BCrypt)
 * - 实验步骤3: XSS 防护 (Thymeleaf th:text 自动转义)
 * - 实验步骤4: IDOR 防护 (权限验证)
 * - 实验步骤5: 安全配置 (自定义错误页面)
 * - 实验步骤7: 功能级别访问控制 (RBAC)
 * - 实验步骤8: CSRF 防护 (Spring Security CSRF tokens)
 * 
 * @author SecureShop Team
 * @version 1.0.0
 */
@SpringBootApplication
public class SecureShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureShopApplication.class, args);
    }
}
