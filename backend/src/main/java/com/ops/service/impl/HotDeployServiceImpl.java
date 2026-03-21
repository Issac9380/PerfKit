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

@Slf4j
@Service
@RequiredArgsConstructor
public class HotDeployServiceImpl implements HotDeployService {

    @Value("${app.upload.path}")
    private String uploadPath;

    private final KubernetesClientFactory clientFactory;
    private final K8sClusterMapper clusterMapper;

    @Override
    public String uploadFile(MultipartFile file) {
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
            (!originalFilename.endsWith(".class") && !originalFilename.endsWith(".jar"))) {
            throw new BusinessException(400, "仅支持 .class 和 .jar 文件");
        }

        // 校验文件大小
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过 50MB");
        }

        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileId = UUID.randomUUID().toString();
            String saveName = fileId + "_" + originalFilename;
            Path filePath = uploadDir.resolve(saveName);
            Files.copy(file.getInputStream(), filePath);

            return fileId;
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

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
