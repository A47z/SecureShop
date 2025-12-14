package com.secureshop.entity;

/**
 * 订单状态枚举
 * 
 * 对应 OWASP 实验步骤7: 功能级别的访问控制
 * 不同状态的订单可能需要不同的操作权限
 */
public enum OrderStatus {
    /**
     * 待支付
     * 允许操作: 用户可以取消、支付
     */
    PENDING,

    /**
     * 已支付
     * 允许操作: 管理员可以发货
     */
    PAID,

    /**
     * 已发货
     * 允许操作: 用户可以确认收货
     */
    SHIPPED,

    /**
     * 已完成
     * 允许操作: 用户可以评价
     */
    COMPLETED,

    /**
     * 已取消
     * 不允许任何修改操作
     */
    CANCELLED
}
