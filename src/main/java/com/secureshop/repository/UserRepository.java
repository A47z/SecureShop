package com.secureshop.repository;

import com.secureshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * 对应 OWASP 实验步骤1: 预防注入攻击
 * 
 * 安全特性:
 * 1. 使用JPA Repository，自动生成参数化查询
 * 2. 避免字符串拼接SQL，防止SQL注入
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * 
     * 实验步骤1: JPA会自动生成参数化查询:
     * SELECT * FROM users WHERE username = ?
     * 
     * @param username 用户名
     * @return Optional<User>
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * 
     * 实验步骤1: 参数化查询，防止SQL注入
     * 
     * @param email 邮箱
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否已存在
     * 
     * 用于注册时验证用户名唯一性
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     * 
     * 用于注册时验证邮箱唯一性
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}
