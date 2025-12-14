package com.secureshop.service;

import com.secureshop.dto.UserRegistrationDto;
import com.secureshop.entity.Role;
import com.secureshop.entity.User;
import com.secureshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务类
 * 
 * 对应 OWASP 实验步骤2: 身份验证和会话管理
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * 
     * 实验步骤2:
     * - 密码使用 BCrypt 哈希后存储
     * - 在对比用户输入和密码时，计算输入的哈希之后比较哈希之后的字符串
     * - 永远不要解密密码来使用纯文本用户输入来比较
     * 
     * @param registrationDto 注册DTO
     * @return 创建的用户
     * @throws IllegalArgumentException 用户名或邮箱已存在
     */
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());

        // 实验步骤2: 使用 BCrypt 哈希密码
        // passwordEncoder.encode() 会生成 BCrypt 哈希值
        // 哈希值包含自动生成的 salt，长度约60字符
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // 设置默认角色为普通用户
        user.setRole(Role.USER);
        user.setEnabled(true);

        // 设置可选字段
        user.setFullName(registrationDto.getFullName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());

        return userRepository.save(user);
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
