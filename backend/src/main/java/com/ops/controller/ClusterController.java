package com.ops.controller;

import com.ops.common.Result;
import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;
import com.ops.service.K8sService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final K8sService k8sService;

    @GetMapping
    public Result<List<K8sCluster>> list() {
        return Result.success(k8sService.list());
    }

    @PostMapping
    public Result<K8sCluster> create(@Valid @RequestBody ClusterRequest request) {
        return Result.success(k8sService.create(request));
    }

    @PutMapping("/{id}")
    public Result<K8sCluster> update(@PathVariable Long id, @Valid @RequestBody ClusterRequest request) {
        return Result.success(k8sService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        k8sService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    public Result<Boolean> test(@PathVariable Long id) {
        return Result.success(k8sService.testConnection(id));
    }

    @GetMapping("/{id}/namespaces")
    public Result<List<String>> getNamespaces(@PathVariable Long id) {
        return Result.success(k8sService.getNamespaces(id));
    }

    @GetMapping("/{id}/pods")
    public Result<List<String>> getPods(@PathVariable Long id, @RequestParam String namespace) {
        return Result.success(k8sService.getPods(id, namespace));
    }

    @GetMapping("/{id}/pods/{pod}/containers")
    public Result<List<String>> getContainers(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace) {
        return Result.success(k8sService.getContainers(id, namespace, pod));
    }

    @GetMapping("/{id}/pods/{pod}/logs")
    public Result<String> getLogs(
            @PathVariable Long id,
            @PathVariable String pod,
            @RequestParam String namespace,
            @RequestParam String container) {
        return Result.success(k8sService.getLogs(id, namespace, pod, container));
    }
}
