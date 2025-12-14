package com.secureshop.repository;

import com.secureshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问层
 * 
 * 对应 OWASP 实验步骤4: 避免直接引用不安全对象 (IDOR)
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 查询用户的所有订单
     * 
     * 实验步骤4: IDOR 防护
     * 在查询时就过滤用户权限
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据订单ID和用户ID查询订单
     * 
     * 实验步骤4: IDOR 防护 - 核心方法
     * 同时验证订单ID和用户ID，防止越权访问
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return Optional<Order>
     */
    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId,
            @Param("userId") Long userId);

    /**
     * 管理员查询所有订单
     * 
     * @return 所有订单列表
     */
    List<Order> findAllByOrderByCreatedAtDesc();
}
