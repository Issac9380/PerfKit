package com.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 用户登录接口的请求参数封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class LoginRequest {
    /**
     * 用户名
     * 登录账号，必填项
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     * 登录密码，必填项
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
