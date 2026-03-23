package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;
import com.ops.service.AuditService;
import com.ops.service.PerformanceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * AnalysisController
 * 处理性能分析相关的HTTP请求，包括命令执行和性能数据采集功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final PerformanceService performanceService;
    private final AuditService auditService;

    /**
     * 执行命令
     * 在目标容器中执行性能分析命令（如 Arthas 命令），并返回执行结果
     *
     * @param request     命令执行请求，包含目标集群、命名空间、Pod、容器及命令内容
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 返回命令执行结果的Result对象
     */
    @PostMapping("/execute")
    public Result<ExecuteCommandResult> execute(@RequestBody ExecuteCommandRequest request, HttpServletRequest httpRequest) {
        ExecuteCommandResult result = performanceService.execute(request);
        auditLog("EXECUTE_COMMAND", "analysis", null, request.toString(), httpRequest);
        return Result.success(result);
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
