package com.secureshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体类
 * 
 * 对应 OWASP 实验步骤4: 避免直接引用不安全对象 (IDOR)
 * 
 * 安全特性:
 * 1. 订单与用户关联，确保只有订单所有者才能访问
 * 2. 在Service层必须验证当前登录用户是否为订单拥有者
 * 3. 使用非直接引用（订单ID）而非用户敏感信息
 * 
 * IDOR漏洞演示场景:
 * - 错误做法: GET /order?id=123 任何人都可以通过修改id访问其他用户订单
 * - 正确做法: 在返回订单前验证 order.user.id == currentUser.id
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单所属用户
     * 
     * 实验步骤4: IDOR防护关键字段
     * 必须在每次访问订单时验证当前用户是否为订单拥有者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "订单必须关联用户")
    private User user;

    /**
     * 订单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * 订单总金额
     * 实验步骤1: 使用BigDecimal确保精确计算
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal totalAmount;

    /**
     * 收货地址
     * 实验步骤6: 敏感数据保护
     */
    @Column(nullable = false, length = 500)
    private String shippingAddress;

    /**
     * 收货人姓名
     * 实验步骤6: 敏感数据
     */
    @Column(nullable = false, length = 100)
    private String receiverName;

    /**
     * 收货人电话
     * 实验步骤6: 敏感数据
     */
    @Column(nullable = false, length = 20)
    private String receiverPhone;

    /**
     * 订单项列表（订单包含的商品）
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 订单备注
     */
    @Column(length = 500)
    private String notes;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 支付时间
     */
    @Column
    private LocalDateTime paidAt;

    /**
     * 发货时间
     */
    @Column
    private LocalDateTime shippedAt;

    /**
     * 完成时间
     */
    @Column
    private LocalDateTime completedAt;

    // 辅助方法

    /**
     * 检查订单是否属于指定用户
     * 
     * 实验步骤4: IDOR防护 - 核心验证方法
     * 在Service层必须使用此方法验证访问权限
     */
    public boolean belongsToUser(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    /**
     * 检查订单是否属于指定用户（重载方法）
     */
    public boolean belongsToUser(User user) {
        return user != null && belongsToUser(user.getId());
    }

    /**
     * 添加订单项
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * 移除订单项
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    /**
     * 计算订单总金额
     */
    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
