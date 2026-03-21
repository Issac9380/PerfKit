package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;
import com.ops.service.AuditService;
import com.ops.service.LogAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/log")
@RequiredArgsConstructor
public class LogController {

    private final LogAnalysisService logAnalysisService;
    private final AuditService auditService;

    @PostMapping("/analyze")
    public Result<LogAnalyzeResult> analyze(@RequestBody LogAnalyzeRequest request, HttpServletRequest httpRequest) {
        LogAnalyzeResult result = logAnalysisService.analyze(request);
        auditLog("LOG_ANALYZE", "log", request.getClusterId(), request.toString(), httpRequest);
        return Result.success(result);
    }

    @GetMapping("/containers/{clusterId}")
    public Result<?> getContainers(@PathVariable Long clusterId, @RequestParam String namespace, HttpServletRequest request) {
        auditLog("LIST_CONTAINERS", "log", clusterId, "namespace=" + namespace, request);
        // TODO: 返回可分析的容器列表
        return Result.success(List.of());
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
