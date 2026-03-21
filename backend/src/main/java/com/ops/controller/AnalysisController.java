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

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final PerformanceService performanceService;
    private final AuditService auditService;

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
