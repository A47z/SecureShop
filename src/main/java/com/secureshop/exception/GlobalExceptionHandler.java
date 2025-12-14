package com.secureshop.exception;

import com.secureshop.exception.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 全局异常处理器
 * 
 * 对应 OWASP 实验步骤5: 基本的安全配置指南
 * 
 * 文档要求:
 * "建立不会泄露跟踪信息、软件版本、程序组件名称，或任何其它调试信息的自定义的错误页面。
 * 如果开发者需要跟踪错误记录或者一些一些标识符对于技术支持非常必要，创建带有简单 ID
 * 和错误描述的索引，并只展示 ID 给用户。"
 * 
 * 安全特性:
 * 1. 不向用户显示堆栈跟踪信息
 * 2. 不泄露软件版本、框架信息
 * 3. 生成唯一的错误ID，便于日志追踪
 * 4. 记录详细错误到日志，但只向用户展示通用错误信息
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理 IDOR 未授权访问异常
     * 
     * 实验步骤4: IDOR 防护
     * 
     * @param ex      异常
     * @param request 请求
     * @return 错误视图
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        // 记录详细错误信息到日志 (供开发者和技术支持查看)
        log.warn("未授权访问 [错误ID: {}] - 用户: {}, 请求路径: {}, 消息: {}",
                errorId,
                request.getRemoteUser(),
                request.getRequestURI(),
                ex.getMessage());

        // 返回通用错误页面 (不泄露详细信息给用户)
        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("errorId", errorId);
        mav.addObject("message", "访问被拒绝");
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    /**
     * 处理 Spring Security 访问拒绝异常
     * 
     * 实验步骤7: 功能级别的访问控制
     * 
     * @param ex      异常
     * @param request 请求
     * @return 错误视图
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        log.warn("访问被拒绝 [错误ID: {}] - 用户: {}, 请求路径: {}",
                errorId,
                request.getRemoteUser(),
                request.getRequestURI());

        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("errorId", errorId);
        mav.addObject("message", "您没有权限访问此资源");
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    /**
     * 处理非法参数异常
     * 
     * @param ex      异常
     * @param request 请求
     * @return 错误视图
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        log.warn("非法参数 [错误ID: {}] - 请求路径: {}, 消息: {}",
                errorId,
                request.getRequestURI(),
                ex.getMessage());

        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("errorId", errorId);
        mav.addObject("message", "请求参数错误");
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    /**
     * 处理所有其他未捕获的异常 (500 错误)
     * 
     * 对应 OWASP 实验步骤5: 自定义错误页面
     * 
     * ===== 安全错误处理原理 =====
     * 
     * ❌ 不安全的做法 - 直接显示堆栈跟踪:
     * 
     * HTTP 500 Internal Server Error
     * 
     * java.lang.NullPointerException: Cannot invoke "User.getId()"
     * because "user" is null
     * at
     * com.secureshop.controller.OrderController.getOrderDetails(OrderController.java:42)
     * at org.springframework.web.method.support.InvocableHandlerMethod.invoke(...)
     * at
     * org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
     * 
     * 问题:
     * - 泄露了代码文件名 (OrderController.java)
     * - 泄露了行号 (42)
     * - 泄露了使用的框架 (Spring Boot)
     * - 泄露了代码逻辑 (user.getId() 为 null)
     * - 攻击者可以利用这些信息构造更精确的攻击
     * 
     * ✅ 安全的做法 - 显示通用错误信息:
     * 
     * 系统错误
     * 
     * 抱歉，系统遇到了一个错误。
     * 
     * 如果问题持续存在，请联系技术支持并提供以下错误ID：
     * 错误ID: 7f3a9b2c-4d1e-4a8f-9c3b-1e5f6a7b8c9d
     * 
     * 优点:
     * - 不泄露任何技术细节
     * - 提供错误ID用于日志追踪
     * - 详细错误信息只记录在服务器日志中
     * - 开发者和技术支持可以通过错误ID查找具体错误
     * 
     * @param ex      异常
     * @param request 请求
     * @return 错误视图
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        // 生成唯一的错误ID
        // 实验步骤5: 创建带有简单 ID 和错误描述的索引
        String errorId = generateErrorId();

        // 记录完整的错误信息到日志 (包含堆栈跟踪)
        // 这些信息只有开发者和技术支持人员能看到
        log.error("系统错误 [错误ID: {}] - 用户: {}, 请求路径: {}, IP: {}",
                errorId,
                request.getRemoteUser() != null ? request.getRemoteUser() : "匿名",
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex // 完整的异常堆栈会被记录到日志
        );

        // 实验步骤5: 只展示 ID 给用户，不泄露技术细节
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorId", errorId);
        mav.addObject("message", "系统遇到了一个错误");
        mav.addObject("timestamp", LocalDateTime.now());

        // ❌ 不要这样做！不要将异常消息或堆栈跟踪发送给用户
        // mav.addObject("exception", ex);
        // mav.addObject("stackTrace", ex.getStackTrace());
        // mav.addObject("exceptionMessage", ex.getMessage());

        return mav;
    }

    /**
     * 生成唯一的错误ID
     * 
     * 用于日志追踪，技术支持可以通过此ID在日志中查找具体错误
     * 
     * @return 错误ID (UUID格式)
     */
    private String generateErrorId() {
        return UUID.randomUUID().toString();
    }
}
