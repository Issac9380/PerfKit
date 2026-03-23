package com.ops.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 热部署服务接口
 * 定义热部署相关的业务逻辑，包括上传部署文件、执行热部署操作等功能
 * 支持在不重启容器的情况下动态更新Java类或方法
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface HotDeployService {
    /**
     * 上传热部署文件
     * 将要热部署的文件上传到服务器，返回文件的唯一标识ID
     *
     * @param file 要上传的部署文件（通常为JAR、CLASS或其他可部署的文件）
     * @return 返回文件在服务器上的唯一标识ID，用于后续部署操作
     */
    String uploadFile(MultipartFile file);

    /**
     * 执行热部署
     * 将已上传的文件部署到指定的Kubernetes容器中，实现热更新
     *
     * @param clusterId 目标集群ID
     * @param namespace 目标命名空间名称
     * @param podName 目标Pod名称
     * @param containerName 目标容器名称
     * @param fileId 已上传文件的唯一标识ID
     * @param className 要热部署的类名（可选，用于精确部署）
     * @param methodName 要热部署的方法名（可选，用于精确部署）
     */
    void deploy(Long clusterId, String namespace, String podName,
                String containerName, String fileId, String className, String methodName);
}
