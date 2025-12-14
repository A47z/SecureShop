package com.secureshop.controller;

import com.secureshop.entity.Order;
import com.secureshop.entity.OrderItem;
import com.secureshop.entity.OrderStatus;
import com.secureshop.entity.Product;
import com.secureshop.entity.User;
import com.secureshop.exception.UnauthorizedAccessException;
import com.secureshop.repository.OrderRepository;
import com.secureshop.repository.ProductRepository;
import com.secureshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单控制器
 * 
 * 对应 OWASP 实验步骤4: 避免直接引用不安全对象 (IDOR)
 * 
 * 本控制器演示如何防止 IDOR (Insecure Direct Object Reference) 漏洞:
 * 1. 获取当前登录用户的ID
 * 2. 查询订单时必须校验订单是否属于当前用户
 * 3. 如果不属于，抛出"Access Denied"异常
 * 
 * @author SecureShop Team
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 显示当前用户的订单列表
     */
    @GetMapping
    public String listOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("orders", orders);

        return "order/list";
    }

    /**
     * 创建测试订单（用于IDOR演示）
     */
    @PostMapping("/create-test")
    public String createTestOrder(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 获取随机商品
        List<Product> allProducts = productRepository.findByActiveTrue();
        if (allProducts.isEmpty()) {
            return "redirect:/orders?error=no_products";
        }

        // 随机选择1-3个商品
        int itemCount = 1 + (int) (Math.random() * 3);
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("Test Address, City, State 12345");
        order.setReceiverName(user.getUsername());
        order.setReceiverPhone("555-TEST");

        // 添加订单项
        for (int i = 0; i < Math.min(itemCount, allProducts.size()); i++) {
            Product product = allProducts.get((int) (Math.random() * allProducts.size()));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(1);
            item.setPriceAtPurchase(product.getPrice());
            item.setProductNameAtPurchase(product.getName());

            order.addOrderItem(item);
            totalAmount = totalAmount.add(product.getPrice());
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        return "redirect:/orders?success=created";
    }

    /**
     * 查看订单详情 - IDOR 防护演示 (使用 @RequestParam)
     * 
     * 对应 OWASP 实验步骤4: 避免直接引用不安全对象
     * 
     * ===== IDOR 漏洞原理 =====
     * 
     * IDOR (Insecure Direct Object Reference) 发生在:
     * 应用允许攻击者通过修改请求参数来访问其他用户的资源
     * 
     * ===== 攻击场景演示 =====
     * 
     * 假设用户A的订单ID是123，用户B的订单ID是124
     * 
     * 场景: 用户A尝试查看用户B的订单
     * 请求: GET /orders/detail?id=124
     * 
     * ❌ 错误做法 - 存在IDOR漏洞:
     * ```java
     * @GetMapping("/detail")
     * public String getOrderDetails(@RequestParam Long id, Model model) {
     * Order order = orderRepository.findById(id)
     * .orElseThrow(() -> new RuntimeException("订单不存在"));
     * model.addAttribute("order", order);
     * return "order/detail";
     * }
     * ```
     * 问题: 没有验证订单是否属于当前用户，用户A可以查看用户B的订单！
     * 
     * ✅ 正确做法 - IDOR 防护:
     * 1. 获取当前登录用户的ID
     * 2. 查询订单时验证 order.user.id == currentUser.id
     * 3. 如果不匹配，抛出 UnauthorizedAccessException
     * 
     * 文档要求:
     * "在传递相应对象之前校验引用，如果请求的用户没有权限来访问，展示通用错误页面"
     * 
     * @param orderId     订单ID
     * @param userDetails 当前登录用户
     * @param model       模型
     * @return 视图名称
     */
    @GetMapping("/detail")
    public String getOrderDetails(
            @RequestParam Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 1. 获取当前登录用户
        User currentUser = getCurrentUser(userDetails);

        // 2. 查询订单
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 3. 【关键步骤】验证订单是否属于当前用户
        // 实验步骤4: 在传递相应对象之前校验引用
        if (!order.belongsToUser(currentUser)) {
            // 文档要求: 展示通用错误信息，不泄露订单是否存在
            throw new UnauthorizedAccessException("无权访问此订单");
        }

        // 4. 验证通过，返回订单详情
        model.addAttribute("order", order);
        return "order/detail";
    }

    /**
     * 查看订单详情 - 推荐方法 (使用 @PathVariable)
     * 
     * 对应 OWASP 实验步骤4: IDOR 防护
     * 
     * 这个方法使用了更安全的查询方式:
     * 直接在数据库查询时就过滤用户权限
     * 
     * @param orderId     订单ID
     * @param userDetails 当前登录用户
     * @param model       模型
     * @return 视图名称
     */
    @GetMapping("/{orderId}")
    public String getOrderDetailsById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 获取当前用户
        User currentUser = getCurrentUser(userDetails);

        // 实验步骤4: 使用组合查询，同时验证订单ID和用户ID
        // 这种方法更安全，在数据库层面就过滤了权限
        Order order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException(
                        // 文档要求: 使用泛化错误信息
                        // 不区分"订单不存在"和"无权访问"，防止信息泄露
                        "订单不存在或无权访问"));

        model.addAttribute("order", order);
        return "order/detail";
    }

    /**
     * 管理员查看所有订单
     * 
     * 实验步骤7: 功能级别的访问控制
     * 
     * 这个方法只能由 ADMIN 角色访问（在 SecurityConfig 中配置）
     * 
     * @param model 模型
     * @return 视图名称
     */
    @GetMapping("/admin/all")
    public String adminListAllOrders(Model model) {
        // 管理员可以查看所有订单
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("orders", orders);
        return "order/admin-list";
    }

    /**
     * IDOR 漏洞演示页面 (仅用于教学)
     * 
     * 展示正确做法和错误做法的对比
     * 
     * @param model 模型
     * @return 视图名称
     */
    @GetMapping("/idor-demo")
    public String idorDemo(Model model) {
        model.addAttribute("title", "IDOR 漏洞防护演示");
        return "order/idor-demo";
    }

    /**
     * 获取当前登录用户
     * 
     * @param userDetails Spring Security 用户详情
     * @return 当前用户实体
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
