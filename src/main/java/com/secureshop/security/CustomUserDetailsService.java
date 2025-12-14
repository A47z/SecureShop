package com.secureshop.security;

import com.secureshop.entity.User;
import com.secureshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情服务
 * 
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 * 
 * 负责从数据库加载用户信息，供Spring Security进行身份验证
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户信息
     * 
     * 实验步骤2:
     * - 永远不要显示用户是否存在或者信息格式是否正确的错误信息
     * - 对不正确的登录请求、不存在的用户使用相同的泛化信息
     * 
     * @param username 用户名或邮箱
     * @return UserDetails 用户详情
     * @throws UsernameNotFoundException 用户不存在时抛出（但错误信息要泛化）
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试通过用户名或邮箱查找用户
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException(
                        // 实验步骤2: 使用泛化的错误信息，不泄露用户是否存在
                        "用户名或密码无效"));

        // 返回Spring Security的UserDetails对象
        return buildUserDetails(user);
    }

    /**
     * 构建Spring Security的UserDetails对象
     * 
     * @param user 用户实体
     * @return UserDetails
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // 已经是BCrypt哈希值
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(!user.getEnabled()) // 使用enabled字段控制账户锁定
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }

    /**
     * 获取用户权限
     * 
     * 实验步骤7: 功能级别的访问控制
     * 将用户角色转换为Spring Security的GrantedAuthority
     * 
     * @param user 用户实体
     * @return 权限集合
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Spring Security权限需要"ROLE_"前缀
        String authority = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * 更新用户最后登录时间
     * 
     * 实验步骤2: 记录最后登录时间，用于检测异常登录行为
     * 
     * @param username 用户名
     */
    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(user);
                });
    }
}
