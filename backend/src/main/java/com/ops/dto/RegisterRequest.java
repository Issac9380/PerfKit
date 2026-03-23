package com.ops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 * 用户注册接口的请求参数封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class RegisterRequest {
    /**
     * 用户名
     * 登录账号，长度3-50个字符，必填项
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度在3-50之间")
    private String username;

    /**
     * 密码
     * 登录密码，长度6-100个字符，必填项
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度在6-100之间")
    private String password;

    /**
     * 邮箱
     * 用户的电子邮箱地址，可选项
     */
    @Email(message = "邮箱格式不正确")
    private String email;
}
