package com.ops.k8s;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kubernetes客户端工厂类
 * 负责创建和管理Kubernetes客户端实例，支持多种认证方式（kubeconfig、token、证书）
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Component
public class KubernetesClientFactory {

    /**
     * Kubernetes客户端缓存，使用集群ID作为键
     * 用于缓存已创建的客户端实例，便于管理和关闭
     */
    private final Map<Long, KubernetesClient> clientCache = new ConcurrentHashMap<>();

    /**
     * JSON解析器
     * 用于解析token和certificate认证方式的配置JSON
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取Kubernetes客户端实例
     * 根据提供的API服务器地址、认证类型和配置创建相应的Kubernetes客户端
     * 支持三种认证方式：kubeconfig、token和证书认证
     *
     * @param apiServer Kubernetes API服务器地址
     * @param authType  认证类型（kubeconfig/token/certificate）
     * @param config    认证配置信息（JSON格式）
     * @return KubernetesClient Kubernetes客户端实例
     */
    public KubernetesClient getClient(String apiServer, String authType, String config) {
        Config configObj = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .build();

        if ("kubeconfig".equals(authType)) {
            // kubeconfig 方式
            configObj = Config.fromKubeconfig(config);
        } else if ("token".equals(authType)) {
            // Token 方式
            Map<String, String> configMap = parseJsonConfig(config);
            configObj = new ConfigBuilder()
                    .withMasterUrl(apiServer)
                    .withOauthToken(configMap.get("token"))
                    .withTrustCerts(true)
                    .build();
        } else if ("certificate".equals(authType)) {
            // 证书方式
            Map<String, String> configMap = parseJsonConfig(config);
            configObj = new ConfigBuilder()
                    .withMasterUrl(apiServer)
                    .withClientCertData(configMap.get("clientCert"))
                    .withClientKeyData(configMap.get("clientKey"))
                    .withCaCertData(configMap.get("caCert"))
                    .withTrustCerts(true)
                    .build();
        }

        return new KubernetesClientBuilder().withConfig(configObj).build();
    }

    /**
     * 解析JSON格式的配置信息
     * 将JSON配置字符串解析为键值对Map
     *
     * @param config JSON格式的配置字符串
     * @return Map 配置键值对
     */
    private Map<String, String> parseJsonConfig(String config) {
        if (config == null || config.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(config, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON config: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 关闭并移除指定集群的Kubernetes客户端
     * 从缓存中移除客户端并调用close方法释放资源
     *
     * @param clusterId 集群的唯一标识ID
     */
    public void closeClient(Long clusterId) {
        KubernetesClient client = clientCache.remove(clusterId);
        if (client != null) {
            client.close();
        }
    }
}
