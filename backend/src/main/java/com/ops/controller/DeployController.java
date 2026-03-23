package com.ops.controller;

import com.ops.common.Result;
import com.ops.service.AuditService;
import com.ops.service.HotDeployService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * DeployController
 * 处理热部署相关的HTTP请求，包括文件上传和热部署执行功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/deploy")
@RequiredArgsConstructor
@Slf4j
public class DeployController {

    private final HotDeployService hotDeployService;
    private final AuditService auditService;

    /**
     * 上传部署文件
     * 将需要热部署的文件上传到服务器，返回文件ID用于后续部署操作
     *
     * @param file    待部署的文件（通常是编译后的Java class文件或jar包）
     * @param request HTTP请求对象
     * @return 返回文件ID的Result对象
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        log.debug("[DeployController.upload] Enter - filename={}", file.getOriginalFilename());
        try {
            String fileId = hotDeployService.uploadFile(file);
            auditLog("FILE_UPLOAD", "deploy", null, "filename=" + file.getOriginalFilename(), request);
            return Result.success(fileId);
        } catch (Exception e) {
            log.error("[DeployController.upload] Error", e);
            throw e;
        }
    }

    /**
     * 热部署
     * 将已上传的文件热部署到目标容器的指定类或方法中，实现无需重启的应用更新
     *
     * @param clusterId     集群ID
     * @param namespace     Kubernetes命名空间
     * @param podName       Pod名称
     * @param containerName 容器名称
     * @param fileId        上传文件返回的文件ID
     * @param className     目标类名（包含完整包路径）
     * @param methodName    目标方法名（可选，用于精确热更新特定方法）
     * @param request       HTTP请求对象
     * @return 返回操作结果的Result对象
     */
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
        log.debug("[DeployController.hotDeploy] Enter - clusterId={}, namespace={}, podName={}, containerName={}, fileId={}, className={}, methodName={}",
                clusterId, namespace, podName, containerName, fileId, className, methodName);
        try {
            hotDeployService.deploy(clusterId, namespace, podName, containerName, fileId, className, methodName);
            auditLog("HOT_DEPLOY", "deploy", clusterId,
                    "namespace=" + namespace + ",pod=" + podName + ",container=" + containerName +
                    ",fileId=" + fileId + ",className=" + className + ",methodName=" + methodName, request);
            return Result.success();
        } catch (Exception e) {
            log.error("[DeployController.hotDeploy] Error", e);
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
