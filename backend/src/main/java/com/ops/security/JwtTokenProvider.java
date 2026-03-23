package com.ops.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT令牌提供者类
 * 负责JWT令牌的生成、解析和验证
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT密钥，从配置文件加载，用于签名JWT令牌
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * JWT令牌过期时间（毫秒），从配置文件加载
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * 获取用于JWT签名 的密钥
     * 使用HMAC-SHA算法对令牌进行签名
     *
     * @return SecretKey 用于签名的密钥对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * 根据用户ID和用户名创建一个签名的JWT令牌，包含过期时间
     *
     * @param userId   用户的唯一标识ID
     * @param username 用户名
     * @return 签名的JWT令牌字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从JWT令牌中解析用户ID
     * 验证令牌有效性并提取其中的用户ID声明
     *
     * @param token JWT令牌字符串
     * @return 用户ID
     * @throws JwtException 如果令牌无效或解析失败
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证JWT令牌的有效性
     * 检查令牌是否由当前服务签发、未过期且格式正确
     *
     * @param token JWT令牌字符串
     * @return 如果令牌有效返回true，否则返回false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
