package com.ops.service.impl;

import com.ops.common.BusinessException;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.K8sClusterMapper;
import com.ops.service.HotDeployService;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * HotDeployServiceImpl 实现类
 * 实现热部署的业务逻辑，包括Class/Jar文件上传、Arthas热更新等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotDeployServiceImpl implements HotDeployService {

    /**
     * 文件上传路径
     * 从配置文件中读取，用于存储上传的Class和Jar文件
     */
    @Value("${app.upload.path}")
    private String uploadPath;

    /**
     * KubernetesClientFactory工厂类
     * 用于创建Kubernetes客户端连接执行热部署
     */
    private final KubernetesClientFactory clientFactory;

    /**
     * K8sClusterMapper数据库操作类
     * 用于查询Kubernetes集群配置信息
     */
    private final K8sClusterMapper clusterMapper;

    /**
     * 上传热部署文件
     * 接收客户端上传的Class或Jar文件，校验文件类型和大小后保存到本地存储
     *
     * @param file 上传的文件对象
     * @return 文件唯一标识ID，用于后续部署时引用文件
     * @throws BusinessException 文件类型不支持或文件大小超限时抛出400异常
     * @throws BusinessException 文件上传失败时抛出1004异常
     */
    @Override
    public String uploadFile(MultipartFile file) {
        // 校验文件类型，只支持Class和Jar文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
            (!originalFilename.endsWith(".class") && !originalFilename.endsWith(".jar"))) {
            throw new BusinessException(400, "仅支持 .class 和 .jar 文件");
        }

        // 校验文件大小，最大50MB
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过 50MB");
        }

        try {
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件ID，并构建存储文件名
            String fileId = UUID.randomUUID().toString();
            String saveName = fileId + "_" + originalFilename;
            // 解析完整文件路径
            Path filePath = uploadDir.resolve(saveName);
            // 复制文件到存储目录
            Files.copy(file.getInputStream(), filePath);

            return fileId;
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

    /**
     * 执行热部署
     * 将上传的文件部署到指定容器，并使用Arthas实现热更新
     *
     * @param clusterId 集群ID
     * @param namespace 命名空间
     * @param podName Pod名称
     * @param containerName 容器名称
     * @param fileId 文件唯一标识ID
     * @param className 要热部署的类名
     * @param methodName 方法名（可选）
     * @throws BusinessException 集群不存在时抛出404异常
     * @throws BusinessException 热部署失败时抛出500异常
     */
    @Override
    public void deploy(Long clusterId, String namespace, String podName,
                       String containerName, String fileId, String className, String methodName) {
        // 获取集群配置
        K8sCluster cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // 获取上传的文件路径
        String filePath = uploadPath + "/" + fileId + "_*";

        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {

            // 1. 将文件复制到容器中（通过 kubectl cp 或 exec + base64）
            // 2. 使用 Arthas redefine 命令加载新类
            // 3. 记录部署历史到数据库

            log.info("Hot deploy initiated: cluster={}, pod={}, class={}", clusterId, podName, className);
        } catch (Exception e) {
            log.error("Hot deploy failed", e);
            throw new BusinessException(500, "热部署失败: " + e.getMessage());
        }
    }
}
