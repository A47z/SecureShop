package com.secureshop.controller;

import com.secureshop.entity.Comment;
import com.secureshop.entity.User;
import com.secureshop.repository.CommentRepository;
import com.secureshop.repository.ProductRepository;
import com.secureshop.repository.UserRepository;
import com.secureshop.util.XssUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器 (留言板)
 * 
 * 对应 OWASP 实验步骤3: 预防跨站脚本 (XSS)
 * 
 * 本控制器演示如何防止 XSS 攻击:
 * 1. 存储时保持原样
 * 2. 展示时在 Thymeleaf 模板中使用 th:text 自动转义
 * 3. 提供过滤工具类用于额外防护
 * 
 * @author SecureShop Team
 */
@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 显示所有评论 (留言板)
     * 
     * @param model 模型
     * @return 视图名称
     */
    @GetMapping
    public String listComments(Model model) {
        List<Comment> comments = commentRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        return "comment/list";
    }

    /**
     * 提交新评论 - XSS 防护演示
     * 
     * 对应 OWASP 实验步骤3: 预防跨站脚本
     * 
     * ===== XSS 防护策略 =====
     * 
     * 1. 存储时保持原样:
     * - 不在存储时进行 HTML 编码
     * - 保存用户的原始输入
     * - 这样可以在不同场景下灵活处理
     * 
     * 2. 展示时自动转义 (关键!):
     * - 在 Thymeleaf 模板中使用 th:text (而非 th:utext)
     * - th:text 会自动进行 HTML 转义
     * - 恶意脚本会被显示为文本，而不是执行
     * 
     * 3. 文档要求:
     * "当你需要将用户提供的信息放在输出页面上时，校验这些数据来防止任何类型代码的插入"
     * "在它插入到输出之前，过滤或合理编码文本"
     * 
     * ===== XSS 攻击场景演示 =====
     * 
     * 场景1: 尝试注入 JavaScript 脚本
     * 用户输入: <script>alert('XSS Attack!')</script>
     * 
     * ❌ 如果使用 th:utext (不安全):
     * <div th:utext="${comment.content}"></div>
     * 结果: 脚本会被执行，弹出警告框
     * 
     * ✅ 使用 th:text (安全):
     * <div th:text="${comment.content}"></div>
     * HTML 输出: &lt;script&gt;alert('XSS Attack!')&lt;/script&gt;
     * 结果: 浏览器显示为文本，不执行脚本
     * 
     * 场景2: 尝试注入 HTML 标签
     * 用户输入: <img src=x onerror=alert('XSS')>
     * 
     * ❌ th:utext: 图片加载失败时执行脚本
     * ✅ th:text: 显示为文本 "&lt;img src=x onerror=alert('XSS')&gt;"
     * 
     * 场景3: 尝试注入样式
     * 用户输入: <style>body{background:red}</style>
     * 
     * ❌ th:utext: 页面背景变红
     * ✅ th:text: 显示为文本
     * 
     * @param comment       评论对象
     * @param bindingResult 验证结果
     * @param userDetails   当前登录用户
     * @param model         模型
     * @return 重定向到评论列表
     */
    @PostMapping
    public String addComment(
            @Valid @ModelAttribute("newComment") Comment comment,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 验证失败
        if (bindingResult.hasErrors()) {
            List<Comment> comments = commentRepository.findAllByOrderByCreatedAtDesc();
            model.addAttribute("comments", comments);
            return "comment/list";
        }

        // 获取当前用户
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 实验步骤3: 可选的额外防护 - 检测危险内容
        // 虽然 Thymeleaf 的 th:text 已经能防止 XSS，但我们可以提前警告用户
        if (XssUtils.containsDangerousContent(comment.getContent())) {
            model.addAttribute("warning", "检测到潜在的危险内容，已自动转义处理");
        }

        // 实验步骤3: 存储时保持原样 (不编码)
        // 原始内容会被保存到数据库
        comment.setUser(currentUser);
        commentRepository.save(comment);

        return "redirect:/comments";
    }

    /**
     * 演示不同的 XSS 防护方法
     * 
     * @param content 原始内容
     * @param model   模型
     * @return 视图名称
     */
    @GetMapping("/demo")
    public String xssDemo(@RequestParam(required = false) String content, Model model) {
        if (content != null && !content.isEmpty()) {
            // 原始内容
            model.addAttribute("original", content);

            // 方法1: HTML 转义 (推荐)
            model.addAttribute("escaped", XssUtils.escapeHtml(content));

            // 方法2: 移除 <script> 标签
            model.addAttribute("scriptRemoved", XssUtils.removeScriptTags(content));

            // 方法3: 移除所有 HTML 标签
            model.addAttribute("tagsStripped", XssUtils.stripHtmlTags(content));

            // 方法4: 综合过滤
            model.addAttribute("sanitized", XssUtils.sanitize(content));

            // 检测是否包含危险内容
            model.addAttribute("isDangerous", XssUtils.containsDangerousContent(content));
        }

        return "comment/xss-demo";
    }
}
