package com.ops.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 继承OncePerRequestFilter，对每个HTTP请求进行JWT令牌验证
 * 从请求头中提取JWT令牌并设置用户认证信息到Spring Security上下文
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT令牌提供者，用于令牌验证和解析
     */
    private final JwtTokenProvider tokenProvider;

    /**
     * 自定义用户详情服务，用于加载用户信息
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * 处理JWT认证的核心过滤器逻辑
     * 从HTTP请求头中提取JWT令牌，验证令牌有效性，
     * 并将用户认证信息设置到Spring Security上下文中
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链，用于继续传递请求
     * @throws ServletException 如果Servlet处理出错
     * @throws IOException      如果I/O操作出错
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求头中提取JWT令牌
     * 从Authorization头中获取Bearer类型的令牌
     *
     * @param request HTTP请求对象
     * @return JWT令牌字符串，如果不存在或格式不正确则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
