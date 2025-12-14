package com.secureshop.controller;

import com.secureshop.dto.UserRegistrationDto;
import com.secureshop.entity.User;
import com.secureshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 认证控制器
 * 
 * 对应 OWASP 实验步骤1: 输入校验
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 * 
 * 处理用户注册和登录
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 显示登录页面
     * 
     * @param error   是否登录失败
     * @param logout  是否刚登出
     * @param expired 会话是否过期
     * @param model   模型
     * @return 视图名称
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String expired,
            Model model) {

        if (error != null) {
            // 实验步骤2: 使用泛化错误信息
            // 不泄露用户是否存在或密码是否正确
            model.addAttribute("error", "用户名或密码无效");
        }

        if (logout != null) {
            model.addAttribute("message", "您已成功登出");
        }

        if (expired != null) {
            model.addAttribute("error", "会话已过期，请重新登录");
        }

        return "auth/login";
    }

    /**
     * 显示注册页面
     * 
     * @param model 模型
     * @return 视图名称
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new UserRegistrationDto());
        return "auth/register";
    }

    /**
     * 处理用户注册
     * 
     * 对应 OWASP 实验步骤1: 输入校验
     * 
     * ===== 输入校验流程 =====
     * 
     * 1. 使用 @Valid 注解触发 Bean Validation
     * 2. UserRegistrationDto 中的各种验证注解会自动执行:
     * - @NotBlank: 验证不为空
     * - @Size: 验证长度范围
     * - @Email: 验证邮箱格式
     * - @Pattern: 使用正则表达式验证格式
     * - @StrongPassword: 自定义强密码验证
     * 
     * 3. 如果验证失败，错误信息会被添加到 BindingResult
     * 
     * 4. 文档要求:
     * "在服务端，这可以由编写我们自己的校验流程来实现，但是最佳选择是
     * 使用语言自己的校验流程，因为它们更加广泛使用并测试过"
     * 
     * ===== 邮箱正则表达式验证 =====
     * 
     * 文档示例 (JavaScript):
     * var email_regex = /^[a-zA-Z0-9._-]+@([a-zA-Z0-9.-]+\.)+[a-zA-Z0-9.-]{2,4}$/;
     * 
     * Java 实现 (在 UserRegistrationDto 中):
     * @Pattern(
     * regexp = "^[a-zA-Z0-9._-]+@([a-zA-Z0-9.-]+\\.)+[a-zA-Z0-9.-]{2,4}$",
     * message = "邮箱格式不符合规范"
     * )
     * 
     * @param registrationDto    注册DTO
     * @param bindingResult      验证结果
     * @param redirectAttributes 重定向属性
     * @param model              模型
     * @return 视图名称或重定向
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationDto") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 实验步骤1: 检查验证结果
        // 如果 @Valid 验证失败，BindingResult 会包含错误信息
        if (bindingResult.hasErrors()) {
            // 返回注册页面，显示验证错误信息
            return "auth/register";
        }

        // 额外验证：两次密码是否一致
        if (!registrationDto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "两次输入的密码不一致");
            return "auth/register";
        }

        try {
            // 调用服务层进行注册
            User user = userService.registerUser(registrationDto);

            // 注册成功
            redirectAttributes.addFlashAttribute("success",
                    "注册成功！请使用您的用户名和密码登录。");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            // 用户名或邮箱已存在
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            // 其他错误
            // 实验步骤5: 不泄露详细错误信息
            model.addAttribute("error", "注册失败，请稍后重试");
            return "auth/register";
        }
    }
}
