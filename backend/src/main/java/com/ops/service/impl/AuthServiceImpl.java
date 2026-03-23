package com.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ops.common.BusinessException;
import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;
import com.ops.entity.User;
import com.ops.mapper.UserMapper;
import com.ops.security.JwtTokenProvider;
import com.ops.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 实现用户登录和注册的业务逻辑
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 用户Mapper
     * 负责用户数据的数据库操作
     */
    private final UserMapper userMapper;

    /**
     * 密码编码器
     * 用于密码的BCrypt加密和验证
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT令牌提供者
     * 用于生成和验证JWT令牌
     */
    private final JwtTokenProvider tokenProvider;

    /**
     * 用户登录
     * 验证用户名和密码，生成JWT令牌
     *
     * @param request 登录请求参数，包含用户名和密码
     * @return JWT令牌字符串
     * @throws BusinessException 用户名不存在、密码错误或账户被禁用时抛出
     */
    @Override
    public String login(LoginRequest request) {
        // 根据用户名查询用户
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", request.getUsername())
        );

        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证密码是否正确
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证账户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(403, "账户已被禁用");
        }

        // 生成并返回JWT令牌
        return tokenProvider.generateToken(user.getId(), user.getUsername());
    }

    /**
     * 用户注册
     * 创建新用户账号
     *
     * @param request 注册请求参数，包含用户名、密码和邮箱
     * @throws BusinessException 用户名已存在时抛出
     */
    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.selectCount(
                new QueryWrapper<User>().eq("username", request.getUsername())
        ) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus("ACTIVE");

        // 插入用户数据
        userMapper.insert(user);
    }
}
