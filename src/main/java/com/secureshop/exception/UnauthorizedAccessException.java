package com.secureshop.exception;

/**
 * 未授权访问异常
 * 
 * 对应 OWASP 实验步骤4: 避免直接引用不安全对象 (IDOR)
 * 
 * 当用户尝试访问不属于自己的资源时抛出此异常
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
