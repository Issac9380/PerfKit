package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;
import com.ops.service.AuditService;
import com.ops.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController
 * 处理用户认证相关的HTTP请求，包括用户登录和注册功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;

    /**
     * 用户登录
     * 验证用户凭据并返回JWT令牌，用于后续请求的身份认证
     *
     * @param request     登录请求，包含用户名和密码
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 返回包含JWT令牌的Result对象
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.debug("[AuthController.login] Enter - username: {}", request.getUsername());
        try {
            String token = authService.login(request);
            auditService.log(null, "LOGIN", "auth", null, request.getUsername(), getClientIp(httpRequest));
            log.debug("[AuthController.login] Success - username: {}", request.getUsername());
            return Result.success(Map.of("token", token));
        } catch (Exception e) {
            log.error("[AuthController.login] Error - username: {}, error: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * 用户注册
     * 创建新用户账号，注册成功后返回成功响应
     *
     * @param request     注册请求，包含用户名、密码等信息
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 返回操作结果的Result对象
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        log.debug("[AuthController.register] Enter - username: {}", request.getUsername());
        try {
            authService.register(request);
            auditService.log(null, "REGISTER", "auth", null, request.getUsername(), getClientIp(httpRequest));
            log.debug("[AuthController.register] Success - username: {}", request.getUsername());
            return Result.success();
        } catch (Exception e) {
            log.error("[AuthController.register] Error - username: {}, error: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
