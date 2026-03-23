package com.ops.config;

import com.ops.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * 配置JWT认证过滤器、密码编码器、认证管理器和安全过滤链
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT认证过滤器，用于验证请求中的JWT令牌
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 创建密码编码器Bean
     * 使用BCrypt算法对用户密码进行加密和验证
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建认证管理器Bean
     * 从AuthenticationConfiguration中获取认证管理器，用于处理用户认证
     *
     * @param config 认证配置对象
     * @return AuthenticationManager 认证管理器实例
     * @throws Exception 如果获取认证管理器失败
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置Spring Security的过滤器链
     * 禁用CSRF保护，配置无状态会话策略，设置URL访问权限，添加JWT认证过滤器
     *
     * @param http HttpSecurity对象，用于配置安全策略
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 如果配置失败
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
