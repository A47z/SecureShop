package com.secureshop.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 强密码验证器实现
 * 
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 * 
 * 实现文档中的强密码策略要求
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int minLength;
    private boolean requireUpperCase;
    private boolean requireLowerCase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    /**
     * 常见密码黑名单
     * 
     * 文档要求: 禁止使用"常见密码"列表中的密码
     * 参考: https://www.teamsid.com/worst-passwords-2015/
     */
    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
            "password", "123456", "123456789", "12345678", "12345",
            "1234567", "password123", "qwerty", "abc123", "111111",
            "123123", "admin", "letmein", "welcome", "monkey",
            "dragon", "master", "sunshine", "princess", "football",
            "password1", "qwerty123", "passw0rd", "admin123");

    /**
     * 正则表达式模式
     */
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.requireUpperCase = constraintAnnotation.requireUpperCase();
        this.requireLowerCase = constraintAnnotation.requireLowerCase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    /**
     * 验证密码强度
     * 
     * 实验步骤2: 建立强密码策略
     * 
     * @param password 密码
     * @param context  验证上下文
     * @return 是否通过验证
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // 禁用默认消息
        context.disableDefaultConstraintViolation();

        // 1. 检查最小长度
        // 文档要求: 至少 8 个字符，推荐 10 个
        if (password.length() < minLength) {
            context.buildConstraintViolationWithTemplate(
                    String.format("密码长度至少需要 %d 个字符", minLength)).addConstraintViolation();
            return false;
        }

        // 2. 检查是否包含大写字母
        // 文档要求: 使用大写和小写字母
        if (requireUpperCase && !UPPERCASE_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "密码必须包含至少一个大写字母").addConstraintViolation();
            return false;
        }

        // 3. 检查是否包含小写字母
        if (requireLowerCase && !LOWERCASE_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "密码必须包含至少一个小写字母").addConstraintViolation();
            return false;
        }

        // 4. 检查是否包含数字
        // 文档要求: 至少使用一个数字
        if (requireDigit && !DIGIT_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "密码必须包含至少一个数字").addConstraintViolation();
            return false;
        }

        // 5. 检查是否包含特殊字符
        // 文档要求: 至少使用一个特殊字符（空格、!、&、#、%等）
        if (requireSpecialChar && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "密码必须包含至少一个特殊字符 (!@#$%^&* 等)").addConstraintViolation();
            return false;
        }

        // 6. 检查是否为常见密码
        // 文档要求: 禁止使用"常见密码"列表中的密码
        String lowerPassword = password.toLowerCase();
        for (String commonPassword : COMMON_PASSWORDS) {
            if (lowerPassword.equals(commonPassword) ||
                    lowerPassword.contains(commonPassword)) {
                context.buildConstraintViolationWithTemplate(
                        "密码过于简单，请使用更复杂的密码").addConstraintViolation();
                return false;
            }
        }

        // 7. 额外检查：连续字符
        if (hasSequentialChars(password)) {
            context.buildConstraintViolationWithTemplate(
                    "密码不能包含连续的字符序列（如 123、abc）").addConstraintViolation();
            return false;
        }

        // 8. 额外检查：重复字符
        if (hasRepeatingChars(password, 3)) {
            context.buildConstraintViolationWithTemplate(
                    "密码不能包含3个或以上的重复字符（如 aaa、111）").addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * 检查是否包含连续字符
     * 
     * 例如: 123, abc, 789
     */
    private boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();
        for (int i = 0; i < lower.length() - 2; i++) {
            char c1 = lower.charAt(i);
            char c2 = lower.charAt(i + 1);
            char c3 = lower.charAt(i + 2);

            // 检查连续的数字或字母
            if ((c2 == c1 + 1 && c3 == c2 + 1) ||
                    (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否包含重复字符
     * 
     * @param password 密码
     * @param count    重复次数阈值
     * @return 是否包含重复字符
     */
    private boolean hasRepeatingChars(String password, int count) {
        for (int i = 0; i < password.length() - count + 1; i++) {
            char c = password.charAt(i);
            boolean isRepeating = true;
            for (int j = 1; j < count; j++) {
                if (password.charAt(i + j) != c) {
                    isRepeating = false;
                    break;
                }
            }
            if (isRepeating) {
                return true;
            }
        }
        return false;
    }
}
