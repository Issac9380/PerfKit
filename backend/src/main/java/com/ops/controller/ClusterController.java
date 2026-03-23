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

import java.util.Arrays;
import java.util.List;

/**
 * ClusterController
 * 处理Kubernetes集群管理相关的HTTP请求，包括集群CRUD、连接测试、资源查询等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final K8sService k8sService;
    private final AuditService auditService;

    /**
     * 获取集群列表
     * 查询所有已注册的Kubernetes集群信息
     *
     * @param request HTTP请求对象
     * @return 返回集群列表的Result对象
     */
    @GetMapping
    public Result<List<K8sCluster>> list(HttpServletRequest request) {
        auditLog("LIST", "cluster", null, null, request);
        return Result.success(k8sService.list());
    }

    /**
     * 创建集群
     * 注册新的Kubernetes集群，保存集群连接配置信息
     *
     * @param request     集群请求对象，包含集群名称、KubeConfig等配置
     * @param httpRequest HTTP请求对象
     * @return 返回创建成功的集群对象
     */
    @PostMapping
    public Result<K8sCluster> create(@Valid @RequestBody ClusterRequest request, HttpServletRequest httpRequest) {
        K8sCluster cluster = k8sService.create(request);
        auditLog("CREATE", "cluster", cluster.getId(), request.toString(), httpRequest);
        return Result.success(cluster);
    }

    /**
     * 更新集群
     * 根据ID更新已有集群的配置信息
     *
     * @param id          集群ID
     * @param request     新的集群配置数据
     * @param httpRequest HTTP请求对象
     * @return 返回更新后的集群对象
     */
    @PutMapping("/{id}")
    public Result<K8sCluster> update(@PathVariable Long id, @Valid @RequestBody ClusterRequest request, HttpServletRequest httpRequest) {
        K8sCluster cluster = k8sService.update(id, request);
        auditLog("UPDATE", "cluster", id, request.toString(), httpRequest);
        return Result.success(cluster);
    }

    /**
     * 删除集群
     * 根据ID删除指定的Kubernetes集群配置
     *
     * @param id      集群ID
     * @param request HTTP请求对象
     * @return 返回操作结果的Result对象
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        k8sService.delete(id);
        auditLog("DELETE", "cluster", id, null, request);
        return Result.success();
    }

    /**
     * 测试集群连接
     * 验证指定集群的连接配置是否有效
     *
     * @param id      集群ID
     * @param request HTTP请求对象
     * @return 返回连接测试结果的Result对象（true表示连接成功）
     */
    @PostMapping("/{id}/test")
    public Result<Boolean> test(@PathVariable Long id, HttpServletRequest request) {
        auditLog("TEST_CONNECTION", "cluster", id, null, request);
        return Result.success(k8sService.testConnection(id));
    }

    /**
     * 获取命名空间列表
     * 查询指定集群下的所有Kubernetes命名空间
     *
     * @param id      集群ID
     * @param request HTTP请求对象
     * @return 返回命名空间列表的Result对象
     */
    @GetMapping("/{id}/namespaces")
    public Result<List<String>> getNamespaces(@PathVariable Long id, HttpServletRequest request) {
        auditLog("LIST_NAMESPACES", "cluster", id, null, request);
        return Result.success(k8sService.getNamespaces(id));
    }

    /**
     * 获取Pod列表
     * 查询指定集群和命名空间下的所有Pod
     *
     * @param id        集群ID
     * @param namespace Kubernetes命名空间
     * @param request   HTTP请求对象
     * @return 返回Pod名称列表的Result对象
     */
    @GetMapping("/{id}/pods")
    public Result<List<String>> getPods(@PathVariable Long id, @RequestParam String namespace, HttpServletRequest request) {
        auditLog("LIST_PODS", "cluster", id, "namespace=" + namespace, request);
        return Result.success(k8sService.getPods(id, namespace));
    }

    /**
     * 获取容器列表
     * 查询指定Pod下的所有容器
     *
     * @param id        集群ID
     * @param pod       Pod名称
     * @param namespace Kubernetes命名空间
     * @param request   HTTP请求对象
     * @return 返回容器名称列表的Result对象
     */
    @GetMapping("/{id}/pods/{pod}/containers")
    public Result<List<String>> getContainers(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace,
            HttpServletRequest request) {
        auditLog("LIST_CONTAINERS", "cluster", id, "pod=" + pod + ",namespace=" + namespace, request);
        return Result.success(k8sService.getContainers(id, namespace, pod));
    }

    /**
     * 获取容器日志
     * 获取指定容器的日志内容
     *
     * @param id        集群ID
     * @param pod       Pod名称
     * @param namespace Kubernetes命名空间
     * @param container 容器名称
     * @param request   HTTP请求对象
     * @return 返回容器日志内容的Result对象
     */
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

    /**
     * 批量获取容器日志
     * 一次性获取多个Pod的容器日志，用于对比分析
     *
     * @param id        集群ID
     * @param namespace Kubernetes命名空间
     * @param pods      多个Pod名称（用逗号分隔）
     * @param request   HTTP请求对象
     * @return 返回合并后的日志内容的Result对象
     */
    @GetMapping("/{id}/pods/batch/logs")
    public Result<String> getBatchLogs(
            @PathVariable Long id,
            @RequestParam String namespace,
            @RequestParam String pods,
            HttpServletRequest request) {
        List<String> podList = Arrays.asList(pods.split(","));
        auditLog("BATCH_GET_LOGS", "cluster", id, "namespace=" + namespace + ",pods=" + pods, request);
        return Result.success(k8sService.getBatchLogs(id, namespace, podList));
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
