package com.secureshop.dto;

import com.secureshop.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册数据传输对象 (DTO)
 * 
 * 对应 OWASP 实验步骤1: 输入校验
 * 对应 OWASP 实验步骤2: 强密码策略
 * 
 * 使用 Bean Validation 注解进行输入校验
 */
@Data
public class UserRegistrationDto {

    /**
     * 用户名
     * 
     * 实验步骤1: 合理校验输入
     * 实验步骤2: 确保用户名对每个用户唯一、大小写敏感
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线和横线")
    private String username;

    /**
     * 邮箱
     * 
     * 实验步骤1: 在客户端和服务端都要校验邮箱格式
     * 
     * 文档示例 (JavaScript):
     * var email_regex = /^[a-zA-Z0-9._-]+@([a-zA-Z0-9.-]+\.)+[a-zA-Z0-9.-]{2,4}$/;
     * 
     * Java 实现:
     * - 使用 @Email 注解（简单验证）
     * - 使用 @Pattern 正则表达式（更严格的验证）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@([a-zA-Z0-9.-]+\\.)+[a-zA-Z0-9.-]{2,4}$", message = "邮箱格式不符合规范")
    private String email;

    /**
     * 密码
     * 
     * 实验步骤2: 建立强密码策略
     * 
     * 使用自定义的 @StrongPassword 验证器
     * 要求:
     * - 至少 10 个字符
     * - 包含大写字母
     * - 包含小写字母
     * - 包含数字
     * - 包含特殊字符
     * - 不能是常见密码
     */
    @NotBlank(message = "密码不能为空")
    @StrongPassword(minLength = 10, requireUpperCase = true, requireLowerCase = true, requireDigit = true, requireSpecialChar = true)
    private String password;

    /**
     * 确认密码
     * 
     * 用于验证两次密码输入是否一致
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 真实姓名 (可选)
     */
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String fullName;

    /**
     * 电话号码 (可选)
     * 
     * 使用正则表达式验证手机号格式
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    /**
     * 验证两次密码是否一致
     * 
     * @return 是否一致
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
