package com.secureshop.repository;

import com.secureshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品数据访问层
 * 
 * 对应 OWASP 实验步骤1: 预防注入攻击
 * 
 * 安全特性:
 * 1. 使用 JPA Repository，自动生成参数化查询
 * 2. 使用 @Query 注解编写 JPQL 参数化查询
 * 3. 绝对禁止字符串拼接 SQL
 * 
 * SQL 注入预防原理:
 * - 参数化查询将 SQL 代码和数据分离
 * - 参数值作为数据传递，不会被解释为 SQL 代码
 * - 即使输入包含恶意 SQL 语句，也只会被当作普通字符串处理
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据商品名称搜索 (模糊匹配)
     * 
     * ✅ 正确做法 - 使用 JPA 方法命名约定
     * 
     * Spring Data JPA 会自动生成参数化查询:
     * SELECT * FROM products WHERE name LIKE ? AND active = ?
     * 
     * 防御原理:
     * - JPA 使用 PreparedStatement
     * - 参数 'name' 会被正确转义
     * - 恶意输入如 "test' OR '1'='1" 会被当作字面字符串，不会执行
     * 
     * @param name 商品名称关键字
     * @return 商品列表
     */
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    /**
     * 根据分类查询商品
     * 
     * ✅ 使用 @Query 注解 + 命名参数
     * 
     * JPQL 查询会被转换为参数化的 SQL 查询:
     * SELECT p FROM Product p WHERE p.category = ?1 AND p.active = ?2
     * 
     * @param category 分类名称
     * @return 商品列表
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.active = true")
    List<Product> findByCategory(@Param("category") String category);

    /**
     * 复杂查询示例 - 根据价格区间搜索
     * 
     * ✅ 使用 JPQL + 多个命名参数
     * 
     * 即使是复杂查询，也使用参数化方式
     * 
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    @Query("SELECT p FROM Product p " +
            "WHERE p.price BETWEEN :minPrice AND :maxPrice " +
            "AND p.active = true " +
            "ORDER BY p.price ASC")
    List<Product> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice);

    /**
     * ❌ 错误示例 - 绝对不要这样做！
     * 
     * 这是 SQL 注入漏洞的典型例子 (仅用于教学，不要在实际代码中使用)
     * 
     * // 错误做法 - 字符串拼接 SQL
     * 
     * @Query(value = "SELECT * FROM products WHERE name LIKE '%" + name + "%'",
     *              nativeQuery = true)
     *              List<Product> unsafeSearch(String name);
     * 
     *              漏洞原理:
     *              - 如果用户输入: test%' OR '1'='1
     *              - 最终 SQL: SELECT * FROM products WHERE name LIKE '%test%' OR
     *              '1'='1%'
     *              - '1'='1' 永远为真，返回所有商品！
     * 
     *              更危险的输入: '; DROP TABLE products; --
     *              - 可能导致数据库表被删除！
     */

    /**
     * 查询所有上架商品
     * 
     * @return 上架商品列表
     */
    List<Product> findByActiveTrue();

    /**
     * 根据分类统计商品数量
     * 
     * ✅ 使用参数化查询
     * 
     * @param category 分类
     * @return 商品数量
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category AND p.active = true")
    Long countByCategory(@Param("category") String category);
}
