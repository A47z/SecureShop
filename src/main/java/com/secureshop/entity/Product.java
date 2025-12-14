package com.secureshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 
 * 对应 OWASP 实验步骤1: 预防注入攻击
 * 用于演示参数化查询的正确使用
 * 
 * 安全特性:
 * 1. 所有查询操作将使用JPA参数化查询，防止SQL注入
 * 2. 输入验证确保数据完整性
 * 3. 使用@Column约束防止无效数据
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品名称
     * 实验步骤1: 输入校验
     * 实验步骤3: XSS防护（Thymeleaf自动转义）
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 200, message = "商品名称长度必须在2-200个字符之间")
    private String name;

    /**
     * 商品描述
     * 实验步骤3: XSS防护 - 在前端使用th:text而非th:utext
     */
    @Column(length = 2000)
    @Size(max = 2000, message = "商品描述不能超过2000个字符")
    private String description;

    /**
     * 商品价格
     * 使用BigDecimal确保精确计算，避免浮点数误差
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 库存数量
     */
    @Column(nullable = false)
    @NotNull(message = "库存数量不能为空")
    private Integer stock = 0;

    /**
     * 商品图片URL
     * 实验步骤6: 如果存储敏感图片，应该放在Web根目录之外
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 商品分类
     */
    @Column(length = 50)
    private String category;

    /**
     * 是否上架
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 辅助方法

    /**
     * 检查是否有足够库存
     */
    public boolean hasStock(int quantity) {
        return this.stock != null && this.stock >= quantity;
    }

    /**
     * 减少库存
     * 实验步骤1: 业务逻辑验证
     */
    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("库存不足");
        }
        this.stock -= quantity;
    }

    /**
     * 增加库存
     */
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
