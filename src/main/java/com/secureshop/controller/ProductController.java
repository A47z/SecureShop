package com.secureshop.controller;

import com.secureshop.entity.Product;
import com.secureshop.repository.ProductRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品控制器
 * 
 * 对应 OWASP 实验步骤1: 预防注入攻击
 * 
 * 本控制器演示如何安全地处理用户输入，防止 SQL 注入攻击
 * 
 * @author SecureShop Team
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductRepository productRepository;

    /**
     * 商品列表页面
     * 
     * @param model 模型
     * @return 视图名称
     */
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productRepository.findByActiveTrue();
        model.addAttribute("products", products);
        return "product/list";
    }

    /**
     * 商品搜索功能 - SQL 注入预防演示
     * 
     * 对应 OWASP 实验步骤1: 预防注入攻击
     * 
     * ===== SQL 注入预防原理 =====
     * 
     * 1. 输入校验:
     * - 使用 @NotBlank 验证输入不为空
     * - 虽然参数化查询已经能防止 SQL 注入，但输入校验是第一道防线
     * 
     * 2. 参数化查询 (关键!):
     * - ProductRepository 使用 JPA，自动生成参数化查询
     * - 用户输入作为参数传递，不会被解释为 SQL 代码
     * 
     * 3. 实验步骤1 文档要求:
     * "对于 SQL 注入，避免拼接输入值为查询十分关键。反之，使用参数化查询。"
     * 
     * ===== 攻击场景演示 =====
     * 
     * 假设用户在搜索框输入恶意字符串:
     * 
     * 场景1: 尝试绕过查询条件
     * 输入: test' OR '1'='1
     * 
     * ❌ 如果使用字符串拼接 SQL (不安全):
     * String sql = "SELECT * FROM products WHERE name LIKE '%" + keyword + "%'";
     * 最终 SQL: SELECT * FROM products WHERE name LIKE '%test' OR '1'='1%'
     * 结果: '1'='1' 永远为真，返回所有商品！
     * 
     * ✅ 使用参数化查询 (安全):
     * JPA 生成: SELECT * FROM products WHERE name LIKE ?
     * 参数值: "%test' OR '1'='1%"
     * 结果: 恶意字符串被当作普通文本，只会搜索包含 "test' OR '1'='1" 的商品
     * 
     * 场景2: 尝试执行危险操作
     * 输入: '; DROP TABLE products; --
     * 
     * ❌ 字符串拼接:
     * 最终 SQL: SELECT * FROM products WHERE name LIKE '%'; DROP TABLE products; --%'
     * 结果: 可能删除整个商品表！
     * 
     * ✅ 参数化查询:
     * 参数值: "%'; DROP TABLE products; --%"
     * 结果: 只是搜索包含这个字符串的商品，不会执行 DROP 命令
     * 
     * @param keyword 搜索关键字
     * @param model   模型
     * @return 视图名称
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            Model model) {

        // 添加关键字到model（即使为空也添加）
        model.addAttribute("keyword", keyword);

        // 如果关键字为空或只有空格，返回空结果
        if (keyword == null || keyword.trim().isEmpty()) {
            model.addAttribute("products", new java.util.ArrayList<>());
            model.addAttribute("resultCount", 0);
            return "product/list";
        }

        // 实验步骤1: 使用参数化查询防止SQL注入
        try {
            List<Product> products = productRepository
                    .findByNameContainingIgnoreCaseAndActiveTrue(keyword.trim());

            model.addAttribute("products", products);
            model.addAttribute("resultCount", products.size());
        } catch (Exception e) {
            // 如果查询出错，返回空结果
            model.addAttribute("products", new java.util.ArrayList<>());
            model.addAttribute("resultCount", 0);
            model.addAttribute("error", "Search failed. Please try again.");
        }

        return "product/list";
    }

    /**
     * 按分类搜索商品
     * 
     * 实验步骤1: 另一个参数化查询示例
     * 
     * @param category 分类名称
     * @param model    模型
     * @return 视图名称
     */
    @GetMapping("/category")
    public String filterByCategory(
            @RequestParam String category,
            Model model) {

        // 使用 @Query 注解的参数化查询
        List<Product> products = productRepository.findByCategory(category);

        model.addAttribute("products", products);
        model.addAttribute("category", category);

        return "product/list";
    }

    /**
     * 按价格区间搜索
     * 
     * 实验步骤1: 多参数的参数化查询
     * 
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param model    模型
     * @return 视图名称
     */
    @GetMapping("/price-range")
    public String filterByPriceRange(
            @RequestParam(defaultValue = "0") BigDecimal minPrice,
            @RequestParam(defaultValue = "999999") BigDecimal maxPrice,
            Model model) {

        // 使用 JPQL 参数化查询
        List<Product> products = productRepository.findByPriceRange(minPrice, maxPrice);

        model.addAttribute("products", products);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "product/list";
    }
}
