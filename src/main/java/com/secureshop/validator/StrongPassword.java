package com.secureshop.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 强密码验证注解
 * 
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 * 
 * 文档要求:
 * 建立强密码策略，强迫用户创建至少满足下列条件的密码：
 * - 至少 8 个字符，推荐 10 个
 * - 使用大写和小写字母
 * - 至少使用一个数字
 * - 至少使用一个特殊字符
 * - 禁止用户名、站点名称等
 * - 禁止使用"常见密码"列表中的密码
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    /**
     * 默认错误消息
     */
    String message() default "密码强度不足。密码必须至少10个字符，包含大小写字母、数字和特殊字符";

    /**
     * 最小长度 (默认10个字符)
     */
    int minLength() default 10;

    /**
     * 是否要求包含大写字母
     */
    boolean requireUpperCase() default true;

    /**
     * 是否要求包含小写字母
     */
    boolean requireLowerCase() default true;

    /**
     * 是否要求包含数字
     */
    boolean requireDigit() default true;

    /**
     * 是否要求包含特殊字符
     */
    boolean requireSpecialChar() default true;

    /**
     * 验证组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
