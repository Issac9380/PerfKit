package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;
import com.ops.service.AuditService;
import com.ops.service.K8sService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final K8sService k8sService;
    private final AuditService auditService;

    @GetMapping
    public Result<List<K8sCluster>> list(HttpServletRequest request) {
        auditLog("LIST", "cluster", null, null, request);
        return Result.success(k8sService.list());
    }

    @PostMapping
    public Result<K8sCluster> create(@Valid @RequestBody ClusterRequest request, HttpServletRequest httpRequest) {
        K8sCluster cluster = k8sService.create(request);
        auditLog("CREATE", "cluster", cluster.getId(), request.toString(), httpRequest);
        return Result.success(cluster);
    }

    @PutMapping("/{id}")
    public Result<K8sCluster> update(@PathVariable Long id, @Valid @RequestBody ClusterRequest request, HttpServletRequest httpRequest) {
        K8sCluster cluster = k8sService.update(id, request);
        auditLog("UPDATE", "cluster", id, request.toString(), httpRequest);
        return Result.success(cluster);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        k8sService.delete(id);
        auditLog("DELETE", "cluster", id, null, request);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    public Result<Boolean> test(@PathVariable Long id, HttpServletRequest request) {
        auditLog("TEST_CONNECTION", "cluster", id, null, request);
        return Result.success(k8sService.testConnection(id));
    }

    @GetMapping("/{id}/namespaces")
    public Result<List<String>> getNamespaces(@PathVariable Long id, HttpServletRequest request) {
        auditLog("LIST_NAMESPACES", "cluster", id, null, request);
        return Result.success(k8sService.getNamespaces(id));
    }

    @GetMapping("/{id}/pods")
    public Result<List<String>> getPods(@PathVariable Long id, @RequestParam String namespace, HttpServletRequest request) {
        auditLog("LIST_PODS", "cluster", id, "namespace=" + namespace, request);
        return Result.success(k8sService.getPods(id, namespace));
    }

    @GetMapping("/{id}/pods/{pod}/containers")
    public Result<List<String>> getContainers(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace,
            HttpServletRequest request) {
        auditLog("LIST_CONTAINERS", "cluster", id, "pod=" + pod + ",namespace=" + namespace, request);
        return Result.success(k8sService.getContainers(id, namespace, pod));
    }

    @GetMapping("/{id}/pods/{pod}/logs")
    public Result<String> getLogs(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace,
            @RequestParam String container,
            HttpServletRequest request) {
        auditLog("GET_LOGS", "cluster", id, "pod=" + pod + ",namespace=" + namespace + ",container=" + container, request);
        return Result.success(k8sService.getLogs(id, namespace, pod, container));
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
