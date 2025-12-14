package com.secureshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * 重定向控制器
 * 
 * 对应 OWASP 实验步骤10: 避免不安全的重定向
 * 
 * 文档要求:
 * "验证应用接收的任何重定向，确保目标 URL 在白名单中"
 */
@Controller
@RequestMapping("/redirect")
public class RedirectController {

    /**
     * 白名单域名列表
     * 
     * 实验步骤10: 只允许重定向到可信域名
     */
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "secureshop.com",
            "www.secureshop.com",
            "localhost",
            "127.0.0.1");

    /**
     * 安全重定向演示页面
     */
    @GetMapping("/demo")
    public String redirectDemo() {
        return "redirect/demo";
    }

    /**
     * 不安全的重定向示例 (仅用于演示，实际生产环境禁用)
     * 
     * ❌ 错误做法: 直接使用用户输入的URL进行重定向
     * 
     * 攻击场景:
     * /redirect/unsafe?url=https://evil.com
     * 用户会被重定向到恶意网站
     * 
     * @param url 目标URL
     * @return 重定向
     */
    @GetMapping("/unsafe")
    public String unsafeRedirect(@RequestParam String url) {
        // 演示目的：显示不安全的重定向
        // 在生产环境中应该禁用此方法
        return "redirect:" + url;
    }

    /**
     * 安全的重定向 - 白名单验证
     * 
     * ✅ 正确做法: 验证目标URL是否在白名单中
     * 
     * 实验步骤10: 避免不安全的重定向
     * 
     * @param url   目标URL
     * @param model 模型
     * @return 重定向或错误页面
     */
    @GetMapping("/safe")
    public String safeRedirect(@RequestParam String url, Model model) {

        // 验证URL是否安全
        if (isUrlSafe(url)) {
            // URL在白名单中，允许重定向
            return "redirect:" + url;
        } else {
            // URL不在白名单中，拒绝重定向
            model.addAttribute("error", "不允许重定向到外部网站");
            model.addAttribute("attemptedUrl", url);
            return "redirect/blocked";
        }
    }

    /**
     * 验证URL是否安全
     * 
     * 实验步骤10: URL白名单验证
     * 
     * @param url 目标URL
     * @return 是否安全
     */
    private boolean isUrlSafe(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            // 解析URL
            URI uri = new URI(url);

            // 1. 如果是相对路径（没有协议和域名），允许
            if (uri.getScheme() == null || uri.getHost() == null) {
                // 相对路径如 "/home", "/products"
                return url.startsWith("/") && !url.contains("//");
            }

            // 2. 检查协议是否为 http 或 https
            String scheme = uri.getScheme().toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return false;
            }

            // 3. 检查域名是否在白名单中
            String host = uri.getHost().toLowerCase();
            for (String allowedDomain : ALLOWED_DOMAINS) {
                if (host.equals(allowedDomain) || host.endsWith("." + allowedDomain)) {
                    return true;
                }
            }

            // 域名不在白名单中
            return false;

        } catch (Exception e) {
            // URL 格式错误
            return false;
        }
    }
}
