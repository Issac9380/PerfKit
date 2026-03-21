package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LoginRequest;
import com.ops.dto.RegisterRequest;
import com.ops.service.AuditService;
import com.ops.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String token = authService.login(request);
        auditService.log(null, "LOGIN", "auth", null, request.getUsername(), getClientIp(httpRequest));
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authService.register(request);
        auditService.log(null, "REGISTER", "auth", null, request.getUsername(), getClientIp(httpRequest));
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
