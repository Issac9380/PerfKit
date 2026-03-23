package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;
import com.ops.service.AuditService;
import com.ops.service.LogAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LogController
 * 处理日志分析相关的HTTP请求，包括日志分析和容器列表查询功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/log")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final LogAnalysisService logAnalysisService;
    private final AuditService auditService;

    /**
     * 日志分析
     * 对指定集群的容器日志进行分析，提取关键信息和异常模式
     *
     * @param request     日志分析请求，包含集群ID、命名空间、容器名等参数
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 返回日志分析结果的Result对象
     */
    @PostMapping("/analyze")
    public Result<LogAnalyzeResult> analyze(@RequestBody LogAnalyzeRequest request, HttpServletRequest httpRequest) {
        log.debug("[LogController.analyze] Enter - request={}", request);
        try {
            LogAnalyzeResult result = logAnalysisService.analyze(request);
            auditLog("LOG_ANALYZE", "log", request.getClusterId(), request.toString(), httpRequest);
            return Result.success(result);
        } catch (Exception e) {
            log.error("[LogController.analyze] Error", e);
            throw e;
        }
    }

    /**
     * 获取容器列表
     * 查询指定集群和命名空间下可用于日志分析的容器列表
     *
     * @param clusterId  集群ID
     * @param namespace  Kubernetes命名空间
     * @param request    HTTP请求对象
     * @return 返回容器列表的Result对象
     */
    @GetMapping("/containers/{clusterId}")
    public Result<?> getContainers(@PathVariable Long clusterId, @RequestParam String namespace, HttpServletRequest request) {
        log.debug("[LogController.getContainers] Enter - clusterId={}, namespace={}", clusterId, namespace);
        try {
            auditLog("LIST_CONTAINERS", "log", clusterId, "namespace=" + namespace, request);
            // TODO: 返回可分析的容器列表
            return Result.success(List.of());
        } catch (Exception e) {
            log.error("[LogController.getContainers] Error", e);
            throw e;
        }
    }

    private void auditLog(String action, String resourceType, Long resourceId, String requestParams, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        String ipAddress = getClientIp(request);
        auditService.log(userId, action, resourceType, resourceId, requestParams, ipAddress);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
