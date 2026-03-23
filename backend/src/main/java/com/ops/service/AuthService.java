package com.ops.service;

import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;

/**
 * 认证服务接口
 * 定义用户认证相关的业务逻辑
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface AuthService {
    /**
     * 用户登录
     * 验证用户名和密码，生成JWT令牌
     *
     * @param request 登录请求参数
     * @return JWT令牌字符串
     */
    String login(LoginRequest request);

    /**
     * 用户注册
     * 创建新用户账号
     *
     * @param request 注册请求参数
     */
    void register(RegisterRequest request);
}
