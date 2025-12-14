package com.secureshop.config;

import com.secureshop.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Security 安全配置类
 * 
 * 对应 OWASP Top 10 多个安全实验步骤:
 * - 实验步骤2: 身份验证和会话管理
 * - 实验步骤5: 基本的安全配置指南
 * - 实验步骤7: 功能级别的访问控制
 * - 实验步骤8: 防止CSRF
 * 
 * @author SecureShop Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * 密码编码器配置
     * 
     * 对应 OWASP 实验步骤2: 身份验证和会话管理
     * 
     * 要求: 使用强哈希算法，例如 bcrypt
     * BCrypt特点:
     * 1. 自动加盐(salt)
     * 2. 计算成本可配置(strength=10，推荐值)
     * 3. 难以使用GPU破解
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength=10: 2^10 = 1024次迭代，在安全性和性能之间取得平衡
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 身份验证提供者配置
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 身份验证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 登录成功处理器
     * 
     * 实验步骤2: 记录最后登录时间
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // 更新最后登录时间
            String username = authentication.getName();
            userDetailsService.updateLastLogin(username);

            // 重定向到主页
            response.sendRedirect("/home");
        };
    }

    /**
     * 安全过滤器链配置 - 核心配置
     * 
     * 包含所有OWASP安全要求的实现
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ============================================
                // 授权配置 - 实验步骤7: 功能级别的访问控制
                // ============================================
                .authorizeHttpRequests(authorize -> authorize
                        // 静态资源公开访问
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 公开页面
                        .requestMatchers("/", "/home", "/about").permitAll()
                        .requestMatchers("/products", "/products/**").permitAll() // 商品浏览

                        // 认证相关页面公开访问
                        .requestMatchers("/login", "/register", "/error").permitAll()

                        // 实验步骤7: 管理员路径只能由ADMIN角色访问
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 用户订单管理需要登录
                        .requestMatchers("/orders/**", "/cart/**").hasAnyRole("USER", "ADMIN")

                        // 实验步骤7: 默认禁止所有访问，之后在显示的授权校验之后允许访问
                        .anyRequest().authenticated())

                // ============================================
                // 登录配置 - 实验步骤2: 身份验证
                // ============================================
                .formLogin(form -> form
                        .loginPage("/login") // 自定义登录页面
                        .loginProcessingUrl("/login") // 登录表单提交的URL
                        .usernameParameter("username") // 用户名参数名
                        .passwordParameter("password") // 密码参数名
                        .successHandler(authenticationSuccessHandler()) // 登录成功处理
                        .failureUrl("/login?error=true") // 登录失败URL
                        .permitAll())

                // ============================================
                // 登出配置
                // ============================================
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true) // 销毁session
                        .deleteCookies("JSESSIONID") // 删除cookies
                        .permitAll())

                // ============================================
                // CSRF防护 - 实验步骤8: 防止CSRF
                // ============================================
                .csrf(csrf -> csrf
                        // 启用CSRF保护（默认开启，这里显式配置）
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // CSRF token存储在cookie中，Thymeleaf可以自动读取并添加到表单
                )

                // ============================================
                // 会话管理 - 实验步骤2: 会话管理
                // ============================================
                .sessionManagement(session -> session
                        // 实验步骤2: 登录成功后迁移Session，防止会话固定攻击
                        .sessionFixation().migrateSession()

                        // Session创建策略: 需要时创建
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

                        // 最大并发会话数: 同一用户只能有1个活动会话
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false) // 新登录会踢掉旧会话
                        .expiredUrl("/login?expired=true"))

                // ============================================
                // 安全响应头 - 实验步骤5: 基本的安全配置指南
                // ============================================
                .headers(headers -> headers
                        // X-Content-Type-Options: nosniff
                        // 防止浏览器MIME类型嗅探
                        .contentTypeOptions(contentType -> contentType.disable())
                        .contentTypeOptions(contentType -> {
                        })

                        // X-Frame-Options: DENY
                        // 防止点击劫持攻击(Clickjacking)
                        .frameOptions(frame -> frame.deny())

                        // X-XSS-Protection: 1; mode=block
                        // 启用浏览器XSS过滤器
                        .xssProtection(xss -> xss
                                .headerValue(
                                        org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))

                        // Strict-Transport-Security (HSTS)
                        // 实验步骤2: 强制使用HTTPS（生产环境启用）
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000) // 1年
                        )

                        // Content-Security-Policy
                        // 防止XSS和数据注入攻击
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:;")))

                // ============================================
                // Remember Me 功能（可选）
                // ============================================
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey") // 唯一密钥
                        .tokenValiditySeconds(86400 * 7) // 7天有效期
                        .rememberMeParameter("remember-me"));

        return http.build();
    }
}
