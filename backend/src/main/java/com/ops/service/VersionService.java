package com.ops.service;

import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.entity.VersionMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 版本管理服务接口
 * 定义版本管理相关的业务逻辑，包括JDK版本管理、Arthas版本管理、
 * 版本映射管理以及将版本部署到容器等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface VersionService {
    /**
     * 获取所有JDK版本列表
     * 查询系统中所有已上传的JDK版本信息
     *
     * @return 返回所有JdkVersion对象的列表
     */
    List<JdkVersion> listJdkVersions();

    /**
     * 上传新的JDK版本
     * 将JDK安装包上传到服务器，作为可部署的JDK版本
     *
     * @param file JDK安装包文件
     * @return 返回创建的JdkVersion对象，包含版本信息
     */
    JdkVersion uploadJdkVersion(MultipartFile file);

    /**
     * 删除指定的JDK版本
     * 根据版本ID删除指定的JDK版本
     *
     * @param id 要删除的JDK版本ID
     */
    void deleteJdkVersion(Long id);

    /**
     * 获取所有Arthas版本列表
     * 查询系统中所有已上传的Arthas版本信息
     *
     * @return 返回所有ArthasVersion对象的列表
     */
    List<ArthasVersion> listArthasVersions();

    /**
     * 上传新的Arthas版本
     * 将Arthas安装包上传到服务器，作为可部署的Arthas版本
     *
     * @param file Arthas安装包文件
     * @return 返回创建的ArthasVersion对象，包含版本信息
     */
    ArthasVersion uploadArthasVersion(MultipartFile file);

    /**
     * 删除指定的Arthas版本
     * 根据版本ID删除指定的Arthas版本
     *
     * @param id 要删除的Arthas版本ID
     */
    void deleteArthasVersion(Long id);

    /**
     * 获取所有版本映射关系
     * 查询系统中所有JDK与Arthas的版本映射关系
     *
     * @return 返回所有VersionMapping对象的列表
     */
    List<VersionMapping> listMappings();

    /**
     * 将指定版本部署到容器
     * 将JDK或Arthas版本部署到指定的Kubernetes容器中
     *
     * @param versionId 要部署的版本ID
     * @param type 版本类型（JDK或ARTHAS）
     * @param clusterId 目标集群ID
     * @param namespace 目标命名空间名称
     * @param podName 目标Pod名称
     * @param containerName 目标容器名称
     */
    void deployToContainer(Long versionId, String type, Long clusterId, String namespace, String podName, String containerName);
}
