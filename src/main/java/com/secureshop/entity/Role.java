package com.secureshop.entity;

/**
 * 用户角色枚举
 * 
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 * 对应 OWASP 实验步骤7: 功能级别的访问控制
 * 
 * 用于实现基于角色的访问控制 (RBAC)
 */
public enum Role {
    /**
     * 普通用户角色
     * 权限: 浏览商品、下单、管理自己的订单
     */
    USER,
    
    /**
     * 管理员角色
     * 权限: 管理商品、查看所有订单、管理用户
     */
    ADMIN
}
