package com.secureshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 评论/留言实体类
 * 
 * 对应 OWASP 实验步骤3: 预防跨站脚本 (XSS)
 * 
 * 安全特性:
 * 1. 存储时保持原样，不做任何过滤或编码
 * 2. 展示时在 Thymeleaf 模板中使用 th:text 自动转义 HTML
 * 3. 使用输入验证防止超长内容
 */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 评论用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 评论关联的商品（可选）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * 评论内容
     * 
     * 实验步骤3: XSS 防护
     * - 存储时保存原始内容（不进行 HTML 编码）
     * - 展示时使用 th:text 自动转义
     */
    @Column(nullable = false, length = 1000)
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论长度必须在1-1000个字符之间")
    private String content;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
