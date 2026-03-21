package com.ops.controller;

import com.ops.common.Result;
import com.ops.service.AuditService;
import com.ops.service.HotDeployService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/deploy")
@RequiredArgsConstructor
public class DeployController {

    private final HotDeployService hotDeployService;
    private final AuditService auditService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String fileId = hotDeployService.uploadFile(file);
        auditLog("FILE_UPLOAD", "deploy", null, "filename=" + file.getOriginalFilename(), request);
        return Result.success(fileId);
    }

    @PostMapping("/hot")
    public Result<Void> hotDeploy(
            @RequestParam Long clusterId,
            @RequestParam String namespace,
            @RequestParam String podName,
            @RequestParam String containerName,
            @RequestParam String fileId,
            @RequestParam String className,
            @RequestParam(required = false) String methodName,
            HttpServletRequest request) {
        hotDeployService.deploy(clusterId, namespace, podName, containerName, fileId, className, methodName);
        auditLog("HOT_DEPLOY", "deploy", clusterId,
                "namespace=" + namespace + ",pod=" + podName + ",container=" + containerName +
                ",fileId=" + fileId + ",className=" + className + ",methodName=" + methodName, request);
        return Result.success();
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
