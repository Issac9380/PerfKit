package com.ops.k8s;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class KubernetesClientFactory {

    private final Map<Long, KubernetesClient> clientCache = new ConcurrentHashMap<>();

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

    private Map<String, String> parseJsonConfig(String config) {
        // 简化实现，实际应使用 Jackson 解析
        return Map.of();
    }

    public void closeClient(Long clusterId) {
        KubernetesClient client = clientCache.remove(clusterId);
        if (client != null) {
            client.close();
        }
    }
}
