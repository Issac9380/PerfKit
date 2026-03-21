package com.ops.service.impl;

import com.ops.common.BusinessException;
import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.ArthasVersionMapper;
import com.ops.mapper.JdkVersionMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.service.VersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    @Value("${app.upload.path}")
    private String uploadPath;

    private final JdkVersionMapper jdkVersionMapper;
    private final ArthasVersionMapper arthasVersionMapper;
    private final K8sClusterMapper clusterMapper;
    private final KubernetesClientFactory clientFactory;

    @Override
    public List<JdkVersion> listJdkVersions() {
        return jdkVersionMapper.selectList(null);
    }

    @Override
    public JdkVersion uploadJdkVersion(MultipartFile file) {
        validateFile(file, "jdk");

        try {
            String fileId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String saveName = fileId + "_" + originalFilename;
            Path filePath = Paths.get(uploadPath, saveName);

            Files.copy(file.getInputStream(), filePath);

            JdkVersion jdkVersion = new JdkVersion();
            jdkVersion.setVersion(extractVersion(originalFilename));
            jdkVersion.setFilePath(filePath.toString());
            jdkVersion.setFileSize(file.getSize());
            jdkVersion.setMd5(calculateMD5(file.getBytes()));

            jdkVersionMapper.insert(jdkVersion);
            return jdkVersion;
        } catch (IOException e) {
            log.error("Failed to upload JDK version", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

    @Override
    public void deleteJdkVersion(Long id) {
        JdkVersion jdkVersion = jdkVersionMapper.selectById(id);
        if (jdkVersion != null) {
            // 删除物理文件
            try {
                Files.deleteIfExists(Paths.get(jdkVersion.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", jdkVersion.getFilePath());
            }
            jdkVersionMapper.deleteById(id);
        }
    }

    @Override
    public List<ArthasVersion> listArthasVersions() {
        return arthasVersionMapper.selectList(null);
    }

    @Override
    public ArthasVersion uploadArthasVersion(MultipartFile file) {
        validateFile(file, "arthas");

        try {
            String fileId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String saveName = fileId + "_" + originalFilename;
            Path filePath = Paths.get(uploadPath, saveName);

            Files.copy(file.getInputStream(), filePath);

            ArthasVersion arthasVersion = new ArthasVersion();
            arthasVersion.setVersion(extractVersion(originalFilename));
            arthasVersion.setFilePath(filePath.toString());
            arthasVersion.setFileSize(file.getSize());
            arthasVersion.setMd5(calculateMD5(file.getBytes()));

            arthasVersionMapper.insert(arthasVersion);
            return arthasVersion;
        } catch (IOException e) {
            log.error("Failed to upload Arthas version", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

    @Override
    public void deleteArthasVersion(Long id) {
        ArthasVersion arthasVersion = arthasVersionMapper.selectById(id);
        if (arthasVersion != null) {
            try {
                Files.deleteIfExists(Paths.get(arthasVersion.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", arthasVersion.getFilePath());
            }
            arthasVersionMapper.deleteById(id);
        }
    }

    @Override
    public void deployToContainer(Long versionId, String type, Long clusterId,
                                   String namespace, String podName, String containerName) {
        // 获取版本信息
        String filePath;
        if ("jdk".equals(type)) {
            JdkVersion jdkVersion = jdkVersionMapper.selectById(versionId);
            if (jdkVersion == null) {
                throw new BusinessException(404, "JDK版本不存在");
            }
            filePath = jdkVersion.getFilePath();
        } else {
            ArthasVersion arthasVersion = arthasVersionMapper.selectById(versionId);
            if (arthasVersion == null) {
                throw new BusinessException(404, "Arthas版本不存在");
            }
            filePath = arthasVersion.getFilePath();
        }

        // 获取集群配置
        K8sCluster cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }

        // TODO: 通过 K8S exec 将文件传输到容器
        log.info("Deploy {} to container: cluster={}, pod={}, container={}, file={}",
                 type, clusterId, podName, containerName, filePath);
    }

    private void validateFile(MultipartFile file, String type) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名无效");
        }

        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过100MB");
        }

        if ("jdk".equals(type)) {
            if (!originalFilename.endsWith(".tar.gz") && !originalFilename.endsWith(".zip")
                && !originalFilename.endsWith(".jdk")) {
                throw new BusinessException(400, "JDK文件格式不支持");
            }
        } else if ("arthas".equals(type)) {
            if (!originalFilename.endsWith(".jar") && !originalFilename.endsWith(".zip")) {
                throw new BusinessException(400, "Arthas文件格式不支持");
            }
        }
    }

    private String extractVersion(String filename) {
        // 从文件名中提取版本号，例如: arthas-3.7.0.jar -> 3.7.0
        String name = filename.replaceAll("\\.(jar|zip|tar\\.gz|jdk)$", "");
        int lastDash = name.lastIndexOf('-');
        if (lastDash > 0) {
            return name.substring(lastDash + 1);
        }
        return name;
    }

    private String calculateMD5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            log.warn("Failed to calculate MD5", e);
            return "";
        }
    }
}
