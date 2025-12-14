package com.secureshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项实体类
 * 
 * 表示订单中的一个商品项
 * 关联订单和商品的中间实体
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属订单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "订单项必须关联订单")
    private Order order;

    /**
     * 关联的商品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "订单项必须关联商品")
    private Product product;

    /**
     * 购买数量
     */
    @Column(nullable = false)
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;

    /**
     * 购买时的商品价格（快照）
     * 
     * 重要: 保存下单时的价格，而不是引用商品的当前价格
     * 这样即使商品价格变动，历史订单的价格也不会改变
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal priceAtPurchase;

    /**
     * 购买时的商品名称（快照）
     * 防止商品名称修改后影响历史订单显示
     */
    @Column(nullable = false, length = 200)
    private String productNameAtPurchase;

    // 辅助方法

    /**
     * 计算小计金额
     * 小计 = 单价 × 数量
     */
    public BigDecimal getSubtotal() {
        if (priceAtPurchase == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
