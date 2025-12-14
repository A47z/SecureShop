package com.secureshop.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * XSS 防护工具类
 * 
 * 对应 OWASP 实验步骤3: 预防跨站脚本
 * 
 * 提供多种 XSS 防护方法：
 * 1. HTML 转义编码
 * 2. 过滤危险字符
 * 3. 移除 HTML 标签
 * 
 * 文档要求:
 * "在 PHP 中，可以使用 filter_var。对于编码，你可以在 PHP 中使用 htmlspecialchars"
 * 这里我们使用 Java 的等价实现
 */
@Component
public class XssUtils {

    // 危险的 HTML 标签正则表达式
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
            "<[^>]+>",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "javascript:",
            Pattern.CASE_INSENSITIVE);

    /**
     * HTML 转义编码 - 推荐方法
     * 
     * 对应文档中的 htmlspecialchars (PHP) 或 System.Web.Security.AntiXss (.NET)
     * 
     * 将特殊字符转换为 HTML 实体:
     * - & → &amp;
     * - < → &lt;
     * - > → &gt;
     * - " → &quot;
     * - ' → &#39;
     * 
     * 示例:
     * 输入: <script>alert('XSS')</script>
     * 输出: &lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;
     * 
     * 浏览器会显示为文本，而不是执行脚本
     * 
     * @param input 原始输入
     * @return HTML 转义后的字符串
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 使用 Spring 的 HtmlUtils.htmlEscape()
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * 过滤危险字符 - 移除 <script> 标签
     * 
     * 对应文档中的 filter_var (PHP)
     * 
     * 警告: 这种方法不够安全，推荐使用 escapeHtml() 方法
     * 攻击者可以使用各种绕过技巧，例如:
     * - <ScRiPt>alert('XSS')</ScRiPt> (大小写混合)
     * - <img src=x onerror=alert('XSS')> (其他标签)
     * 
     * @param input 原始输入
     * @return 移除 <script> 标签后的字符串
     */
    public static String removeScriptTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 移除 <script> 标签
        return SCRIPT_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * 移除所有 HTML 标签
     * 
     * 适用于只允许纯文本的场景（如用户名、标题）
     * 
     * 示例:
     * 输入: Hello <b>World</b><script>alert('XSS')</script>
     * 输出: Hello World
     * 
     * @param input 原始输入
     * @return 移除所有 HTML 标签后的字符串
     */
    public static String stripHtmlTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 先移除 <script> 标签
        String cleaned = SCRIPT_PATTERN.matcher(input).replaceAll("");
        // 再移除所有 HTML 标签
        cleaned = HTML_TAG_PATTERN.matcher(cleaned).replaceAll("");
        return cleaned.trim();
    }

    /**
     * 移除 JavaScript 事件处理器
     * 
     * 防止如下攻击:
     * <img src=x onerror=alert('XSS')>
     * <a href="javascript:alert('XSS')">点击</a>
     * 
     * @param input 原始输入
     * @return 移除 JavaScript 后的字符串
     */
    public static String removeJavaScript(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return JAVASCRIPT_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * 严格过滤 - 综合所有过滤方法
     * 
     * 适用于高安全要求的场景
     * 
     * @param input 原始输入
     * @return 严格过滤后的字符串
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String cleaned = input;

        // 1. 移除 <script> 标签
        cleaned = removeScriptTags(cleaned);

        // 2. 移除 JavaScript 协议
        cleaned = removeJavaScript(cleaned);

        // 3. 移除所有 HTML 标签
        cleaned = stripHtmlTags(cleaned);

        return cleaned.trim();
    }

    /**
     * 验证是否包含危险内容
     * 
     * 用于在存储前检查输入
     * 
     * @param input 输入字符串
     * @return 如果包含危险内容返回 true
     */
    public static boolean containsDangerousContent(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        // 检查是否包含 <script> 标签
        if (SCRIPT_PATTERN.matcher(input).find()) {
            return true;
        }

        // 检查是否包含 javascript: 协议
        if (JAVASCRIPT_PATTERN.matcher(input).find()) {
            return true;
        }

        // 检查常见的 XSS 攻击模式
        String lowerInput = input.toLowerCase();
        String[] dangerousPatterns = {
                "onerror=",
                "onload=",
                "onclick=",
                "onmouseover=",
                "<iframe",
                "<object",
                "<embed"
        };

        for (String pattern : dangerousPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}
