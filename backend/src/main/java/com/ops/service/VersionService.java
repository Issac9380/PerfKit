package com.ops.service;

import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.entity.VersionMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VersionService {
    // JDK 版本管理
    List<JdkVersion> listJdkVersions();
    JdkVersion uploadJdkVersion(MultipartFile file);
    void deleteJdkVersion(Long id);

    // Arthas 版本管理
    List<ArthasVersion> listArthasVersions();
    ArthasVersion uploadArthasVersion(MultipartFile file);
    void deleteArthasVersion(Long id);

    // 版本映射管理
    List<VersionMapping> listMappings();

    // 部署到容器
    void deployToContainer(Long versionId, String type, Long clusterId, String namespace, String podName, String containerName);
}
