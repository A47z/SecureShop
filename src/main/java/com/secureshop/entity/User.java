package com.secureshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 * 
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 * 对应 OWASP 实验步骤6: 敏感数据保护
 * 
 * 安全特性:
 * 1. 密码字段长度为255，用于存储BCrypt哈希值（60字符）
 * 2. 密码永远不以明文存储，只存储哈希值
 * 3. 包含角色字段用于访问控制
 * 4. 使用@Column注解确保数据完整性
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名 - 唯一且不可为空
     * 实验步骤2: 确保用户名对每个用户唯一
     */
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    /**
     * 邮箱 - 唯一且不可为空
     * 实验步骤1: 使用正则表达式校验邮箱格式
     */
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 密码哈希值 - 不可为空
     * 
     * 实验步骤2: 密码不能以纯文本格式储存在数据库中
     * 实验步骤6: 使用强哈希算法，例如 bcrypt
     * 
     * BCrypt 生成的哈希值格式: $2a$10$[22字符salt][31字符hash] = 60字符
     * 为了安全起见，设置为255字符，以便将来可能更换更强的算法
     */
    @Column(nullable = false, length = 255)
    private String password;
    
    /**
     * 用户角色
     * 
     * 实验步骤2: 身份验证和会话管理
     * 实验步骤7: 功能级别的访问控制
     * 
     * 使用枚举类型确保角色值的有效性
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER; // 默认为普通用户
    
    /**
     * 账户是否激活
     * 实验步骤2: 可以用于实现账户锁定机制
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 真实姓名 - 可选
     * 实验步骤6: 敏感数据，如果需要更高安全性可以加密存储
     */
    @Column(length = 100)
    private String fullName;
    
    /**
     * 电话号码 - 可选
     * 实验步骤6: 敏感数据
     */
    @Column(length = 20)
    private String phoneNumber;
    
    /**
     * 收货地址 - 可选
     * 实验步骤6: 敏感数据
     */
    @Column(length = 500)
    private String address;
    
    /**
     * 用户的订单列表
     * 
     * 实验步骤4: 不安全对象的直接引用 (IDOR)
     * 用于演示订单只能由拥有者访问
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    /**
     * 创建时间
     * 使用@CreationTimestamp自动设置
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     * 使用@UpdateTimestamp自动更新
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 最后登录时间
     * 实验步骤2: 可用于检测异常登录行为
     */
    @Column
    private LocalDateTime lastLoginAt;
    
    // 辅助方法
    
    /**
     * 检查用户是否为管理员
     * 实验步骤7: 功能级别的访问控制
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
    
    /**
     * 检查用户是否为普通用户
     */
    public boolean isUser() {
        return this.role == Role.USER;
    }
}
