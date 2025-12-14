package com.secureshop.repository;

import com.secureshop.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论数据访问层
 * 
 * 对应 OWASP 实验步骤3: 预防跨站脚本 (XSS)
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 查询商品的所有评论
     * 
     * @param productId 商品ID
     * @return 评论列表
     */
    List<Comment> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * 查询所有评论（按创建时间倒序）
     * 
     * @return 评论列表
     */
    List<Comment> findAllByOrderByCreatedAtDesc();
}
