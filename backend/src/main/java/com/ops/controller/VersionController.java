package com.ops.controller;

import com.ops.common.Result;
import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.entity.VersionMapping;
import com.ops.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * VersionController
 * 处理版本管理相关的HTTP请求，包括JDK版本、Arthas版本的管理及版本映射和部署功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    /**
     * 获取JDK版本列表
     * 查询所有已上传的JDK版本信息
     *
     * @return 返回JDK版本列表的Result对象
     */
    @GetMapping("/jdk")
    public Result<List<JdkVersion>> listJdkVersions() {
        return Result.success(versionService.listJdkVersions());
    }

    /**
     * 上传JDK版本
     * 将JDK安装包上传到服务器，用于后续部署到目标容器
     *
     * @param file JDK安装包文件
     * @return 返回上传成功的JDK版本对象
     */
    @PostMapping("/jdk")
    public Result<JdkVersion> uploadJdkVersion(@RequestParam("file") MultipartFile file) {
        return Result.success(versionService.uploadJdkVersion(file));
    }

    /**
     * 删除JDK版本
     * 根据ID删除指定的JDK版本
     *
     * @param id JDK版本ID
     * @return 返回操作结果的Result对象
     */
    @DeleteMapping("/jdk/{id}")
    public Result<Void> deleteJdkVersion(@PathVariable Long id) {
        versionService.deleteJdkVersion(id);
        return Result.success();
    }

    /**
     * 获取Arthas版本列表
     * 查询所有已上传的Arthas版本信息
     *
     * @return 返回Arthas版本列表的Result对象
     */
    @GetMapping("/arthas")
    public Result<List<ArthasVersion>> listArthasVersions() {
        return Result.success(versionService.listArthasVersions());
    }

    /**
     * 上传Arthas版本
     * 将Arthas工具包上传到服务器，用于后续部署到目标容器
     *
     * @param file Arthas安装包文件
     * @return 返回上传成功的Arthas版本对象
     */
    @PostMapping("/arthas")
    public Result<ArthasVersion> uploadArthasVersion(@RequestParam("file") MultipartFile file) {
        return Result.success(versionService.uploadArthasVersion(file));
    }

    /**
     * 删除Arthas版本
     * 根据ID删除指定的Arthas版本
     *
     * @param id Arthas版本ID
     * @return 返回操作结果的Result对象
     */
    @DeleteMapping("/arthas/{id}")
    public Result<Void> deleteArthasVersion(@PathVariable Long id) {
        versionService.deleteArthasVersion(id);
        return Result.success();
    }

    /**
     * 获取版本映射列表
     * 查询JDK版本与Arthas版本之间的映射关系
     *
     * @return 返回版本映射列表的Result对象
     */
    @GetMapping("/mappings")
    public Result<List<VersionMapping>> listMappings() {
        return Result.success(versionService.listMappings());
    }

    /**
     * 部署版本到容器
     * 将指定版本（JDK或Arthas）部署到目标容器的指定位置
     *
     * @param versionId     版本ID（JDK或Arthas版本）
     * @param type          版本类型（jdk或arthas）
     * @param clusterId     集群ID
     * @param namespace     Kubernetes命名空间
     * @param podName       Pod名称
     * @param containerName 容器名称
     * @return 返回操作结果的Result对象
     */
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
