package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    // JDK 版本管理
    @GetMapping("/jdk")
    public Result<List<JdkVersion>> listJdkVersions() {
        return Result.success(versionService.listJdkVersions());
    }

    @PostMapping("/jdk")
    public Result<JdkVersion> uploadJdkVersion(@RequestParam("file") MultipartFile file) {
        return Result.success(versionService.uploadJdkVersion(file));
    }

    @DeleteMapping("/jdk/{id}")
    public Result<Void> deleteJdkVersion(@PathVariable Long id) {
        versionService.deleteJdkVersion(id);
        return Result.success();
    }

    // Arthas 版本管理
    @GetMapping("/arthas")
    public Result<List<ArthasVersion>> listArthasVersions() {
        return Result.success(versionService.listArthasVersions());
    }

    @PostMapping("/arthas")
    public Result<ArthasVersion> uploadArthasVersion(@RequestParam("file") MultipartFile file) {
        return Result.success(versionService.uploadArthasVersion(file));
    }

    @DeleteMapping("/arthas/{id}")
    public Result<Void> deleteArthasVersion(@PathVariable Long id) {
        versionService.deleteArthasVersion(id);
        return Result.success();
    }

    // 部署到容器
    @PostMapping("/deploy")
    public Result<Void> deployToContainer(
            @RequestParam Long versionId,
            @RequestParam String type,
            @RequestParam Long clusterId,
            @RequestParam String namespace,
            @RequestParam String podName,
            @RequestParam String containerName) {
        versionService.deployToContainer(versionId, type, clusterId, namespace, podName, containerName);
        return Result.success();
    }
}
