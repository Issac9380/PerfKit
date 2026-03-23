package com.ops.service.impl;

import com.ops.common.BusinessException;
import com.ops.entity.ArthasVersion;
import com.ops.entity.JdkVersion;
import com.ops.entity.K8sCluster;
import com.ops.entity.VersionMapping;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.ArthasVersionMapper;
import com.ops.mapper.JdkVersionMapper;
import com.ops.mapper.K8sClusterMapper;
import com.ops.mapper.VersionMappingMapper;
import com.ops.service.VersionService;
import jakarta.annotation.PostConstruct;
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
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * VersionServiceImpl 实现类
 * 实现版本管理的业务逻辑，包括JDK/Arthas版本的上传、删除、版本映射管理、部署到容器等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    /**
     * 文件上传路径
     * 从配置文件中读取，用于存储上传的JDK和Arthas版本文件
     */
    @Value("${app.upload.path}")
    private String uploadPath;

    /**
     * JdkVersionMapper数据库操作类
     * 用于JDK版本的持久化操作
     */
    private final JdkVersionMapper jdkVersionMapper;

    /**
     * ArthasVersionMapper数据库操作类
     * 用于Arthas版本的持久化操作
     */
    private final ArthasVersionMapper arthasVersionMapper;

    /**
     * K8sClusterMapper数据库操作类
     * 用于查询Kubernetes集群配置信息
     */
    private final K8sClusterMapper clusterMapper;

    /**
     * KubernetesClientFactory工厂类
     * 用于创建Kubernetes客户端连接部署版本
     */
    private final KubernetesClientFactory clientFactory;

    /**
     * VersionMappingMapper数据库操作类
     * 用于版本映射关系的持久化操作
     */
    private final VersionMappingMapper versionMappingMapper;

    /**
     * 初始化默认版本映射关系
     * 在服务启动时检查数据库是否为空，如果为空则插入预设的JDK-Arthas版本映射关系
     */
    @PostConstruct
    public void initDefaultMappings() {
        // 初始化预设的版本映射关系
        if (versionMappingMapper.selectCount(null) == 0) {
            // 创建常用的JDK-Arthas版本映射
            List<VersionMapping> defaultMappings = List.of(
                createMapping("8", "3.7.2", "JDK 8 推荐使用 Arthas 3.7.2", true),
                createMapping("11", "3.7.2", "JDK 11 推荐使用 Arthas 3.7.2", true),
                createMapping("17", "3.7.2", "JDK 17 推荐使用 Arthas 3.7.2", true),
                createMapping("21", "3.7.2", "JDK 21 推荐使用 Arthas 3.7.2", true),
                createMapping("8", "3.7.1", "JDK 8 可用", false),
                createMapping("11", "3.7.1", "JDK 11 可用", false),
                createMapping("17", "3.7.1", "JDK 17 可用", false),
                createMapping("21", "3.7.1", "JDK 21 可用", false),
                createMapping("8", "3.6.8", "JDK 8 兼容版本", false),
                createMapping("11", "3.6.8", "JDK 11 兼容版本", false),
                createMapping("17", "3.6.8", "JDK 17 兼容版本", false)
            );
            // 批量插入到数据库
            defaultMappings.forEach(versionMappingMapper::insert);
            log.info("Initialized default version mappings");
        }
    }

    /**
     * 创建版本映射实体
     * 辅助方法，用于构建VersionMapping对象
     *
     * @param jdkVersion JDK版本号
     * @param arthasVersion Arthas版本号
     * @param desc 描述信息
     * @param recommended 是否推荐
     * @return 版本映射实体对象
     */
    private VersionMapping createMapping(String jdkVersion, String arthasVersion, String desc, boolean recommended) {
        VersionMapping mapping = new VersionMapping();
        mapping.setJdkVersion(jdkVersion);
        mapping.setArthasVersion(arthasVersion);
        mapping.setDescription(desc);
        mapping.setRecommended(recommended);
        mapping.setCreatedAt(LocalDateTime.now());
        return mapping;
    }

    /**
     * 获取所有JDK版本列表
     *
     * @return JDK版本实体列表
     */
    @Override
    public List<JdkVersion> listJdkVersions() {
        return jdkVersionMapper.selectList(null);
    }

    /**
     * 上传JDK版本文件
     * 接收客户端上传的JDK安装包，校验文件格式后保存到本地存储
     *
     * @param file 上传的JDK文件对象
     * @return 创建的JDK版本实体对象
     * @throws BusinessException 文件校验失败时抛出相应异常
     * @throws BusinessException 文件上传失败时抛出1004异常
     */
    @Override
    public JdkVersion uploadJdkVersion(MultipartFile file) {
        // 校验文件
        validateFile(file, "jdk");

        try {
            // 生成唯一文件ID
            String fileId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String saveName = fileId + "_" + originalFilename;
            // 构建文件存储路径
            Path filePath = Paths.get(uploadPath, saveName);
            // 复制文件到存储目录
            Files.copy(file.getInputStream(), filePath);

            // 创建JDK版本实体
            JdkVersion jdkVersion = new JdkVersion();
            // 从文件名提取版本号
            jdkVersion.setVersion(extractVersion(originalFilename));
            jdkVersion.setFilePath(filePath.toString());
            jdkVersion.setFileSize(file.getSize());
            // 计算文件MD5值用于校验
            jdkVersion.setMd5(calculateMD5(file.getBytes()));

            // 持久化到数据库
            jdkVersionMapper.insert(jdkVersion);
            return jdkVersion;
        } catch (IOException e) {
            log.error("Failed to upload JDK version", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

    /**
     * 删除JDK版本
     * 删除数据库记录的同时删除物理文件
     *
     * @param id JDK版本ID
     */
    @Override
    public void deleteJdkVersion(Long id) {
        // 查询版本信息
        JdkVersion jdkVersion = jdkVersionMapper.selectById(id);
        if (jdkVersion != null) {
            // 删除物理文件
            try {
                Files.deleteIfExists(Paths.get(jdkVersion.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", jdkVersion.getFilePath());
            }
            // 删除数据库记录
            jdkVersionMapper.deleteById(id);
        }
    }

    /**
     * 获取所有Arthas版本列表
     *
     * @return Arthas版本实体列表
     */
    @Override
    public List<ArthasVersion> listArthasVersions() {
        return arthasVersionMapper.selectList(null);
    }

    /**
     * 上传Arthas版本文件
     * 接收客户端上传的Arthas安装包，校验文件格式后保存到本地存储
     *
     * @param file 上传的Arthas文件对象
     * @return 创建的Arthas版本实体对象
     * @throws BusinessException 文件校验失败时抛出相应异常
     * @throws BusinessException 文件上传失败时抛出1004异常
     */
    @Override
    public ArthasVersion uploadArthasVersion(MultipartFile file) {
        // 校验文件
        validateFile(file, "arthas");

        try {
            // 生成唯一文件ID
            String fileId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String saveName = fileId + "_" + originalFilename;
            // 构建文件存储路径
            Path filePath = Paths.get(uploadPath, saveName);
            // 复制文件到存储目录
            Files.copy(file.getInputStream(), filePath);

            // 创建Arthas版本实体
            ArthasVersion arthasVersion = new ArthasVersion();
            // 从文件名提取版本号
            arthasVersion.setVersion(extractVersion(originalFilename));
            arthasVersion.setFilePath(filePath.toString());
            arthasVersion.setFileSize(file.getSize());
            // 计算文件MD5值用于校验
            arthasVersion.setMd5(calculateMD5(file.getBytes()));

            // 持久化到数据库
            arthasVersionMapper.insert(arthasVersion);
            return arthasVersion;
        } catch (IOException e) {
            log.error("Failed to upload Arthas version", e);
            throw new BusinessException(1004, "文件上传失败");
        }
    }

    /**
     * 删除Arthas版本
     * 删除数据库记录的同时删除物理文件
     *
     * @param id Arthas版本ID
     */
    @Override
    public void deleteArthasVersion(Long id) {
        // 查询版本信息
        ArthasVersion arthasVersion = arthasVersionMapper.selectById(id);
        if (arthasVersion != null) {
            // 删除物理文件
            try {
                Files.deleteIfExists(Paths.get(arthasVersion.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", arthasVersion.getFilePath());
            }
            // 删除数据库记录
            arthasVersionMapper.deleteById(id);
        }
    }

    /**
     * 获取所有版本映射关系列表
     *
     * @return 版本映射关系实体列表
     */
    @Override
    public List<VersionMapping> listMappings() {
        return versionMappingMapper.selectList(null);
    }

    /**
     * 部署版本到容器
     * 将JDK或Arthas版本文件部署到指定的Kubernetes容器中
     *
     * @param versionId 版本ID
     * @param type 版本类型（jdk或arthas）
     * @param clusterId 集群ID
     * @param namespace 命名空间
     * @param podName Pod名称
     * @param containerName 容器名称
     * @throws BusinessException 版本或集群不存在时抛出404异常
     */
    @Override
    public void deployToContainer(Long versionId, String type, Long clusterId,
                                   String namespace, String podName, String containerName) {
        // 获取版本信息
        String filePath;
        if ("jdk".equals(type)) {
            // 查询JDK版本
            JdkVersion jdkVersion = jdkVersionMapper.selectById(versionId);
            if (jdkVersion == null) {
                throw new BusinessException(404, "JDK版本不存在");
            }
            filePath = jdkVersion.getFilePath();
        } else {
            // 查询Arthas版本
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

    /**
     * 校验上传文件
     * 检查文件是否为空、文件名是否有效、文件大小是否超限、文件格式是否支持
     *
     * @param file 上传的文件对象
     * @param type 文件类型（jdk或arthas）
     * @throws BusinessException 校验失败时抛出相应异常
     */
    private void validateFile(MultipartFile file, String type) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 检查文件名是否有效
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名无效");
        }

        // 检查文件大小，最大100MB
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过100MB");
        }

        // 根据类型校验文件格式
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

    /**
     * 从文件名提取版本号
     * 例如: arthas-3.7.0.jar -> 3.7.0, jdk-17 -> 17
     *
     * @param filename 文件名
     * @return 提取的版本号字符串
     */
    private String extractVersion(String filename) {
        // 移除文件扩展名
        String name = filename.replaceAll("\\.(jar|zip|tar\\.gz|jdk)$", "");
        // 查找最后一个短横线位置
        int lastDash = name.lastIndexOf('-');
        if (lastDash > 0) {
            // 返回短横线后的部分作为版本号
            return name.substring(lastDash + 1);
        }
        return name;
    }

    /**
     * 计算文件的MD5值
     * 用于文件完整性校验
     *
     * @param data 文件字节数组
     * @return MD5十六进制字符串，计算失败返回空字符串
     */
    private String calculateMD5(byte[] data) {
        try {
            // 创建MD5摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算摘要
            byte[] digest = md.digest(data);
            // 转换为十六进制字符串
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            log.warn("Failed to calculate MD5", e);
            return "";
        }
    }
}
