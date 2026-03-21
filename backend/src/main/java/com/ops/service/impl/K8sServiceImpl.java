package com.ops.service.impl;

import com.ops.common.BusinessException;
import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;
import com.ops.k8s.KubernetesClientFactory;
import com.ops.mapper.K8sClusterMapper;
import com.ops.service.K8sService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class K8sServiceImpl implements K8sService {

    private final K8sClusterMapper clusterMapper;
    private final KubernetesClientFactory clientFactory;

    @Override
    public List<K8sCluster> list() {
        return clusterMapper.selectList(null);
    }

    @Override
    public K8sCluster create(ClusterRequest request) {
        K8sCluster cluster = new K8sCluster();
        cluster.setName(request.getName());
        cluster.setApiServer(request.getApiServer());
        cluster.setAuthType(request.getAuthType());
        cluster.setConfig(request.getConfig());
        cluster.setDescription(request.getDescription());
        cluster.setStatus("ACTIVE");
        clusterMapper.insert(cluster);
        return cluster;
    }

    @Override
    public K8sCluster update(Long id, ClusterRequest request) {
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        cluster.setName(request.getName());
        cluster.setApiServer(request.getApiServer());
        cluster.setAuthType(request.getAuthType());
        cluster.setConfig(request.getConfig());
        cluster.setDescription(request.getDescription());
        clusterMapper.updateById(cluster);
        return cluster;
    }

    @Override
    public void delete(Long id) {
        clusterMapper.deleteById(id);
        clientFactory.closeClient(id);
    }

    @Override
    public boolean testConnection(Long id) {
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            client.namespaces().list();
            return true;
        } catch (Exception e) {
            log.error("K8S connection test failed", e);
            return false;
        }
    }

    @Override
    public List<String> getNamespaces(Long id) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            return client.namespaces().list().getItems().stream()
                    .map(ns -> ns.getMetadata().getName())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getPods(Long id, String namespace) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
            return pods.stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getContainers(Long id, String namespace, String podName) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
            return pod.getSpec().getContainers().stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String getLogs(Long id, String namespace, String podName, String containerName) {
        K8sCluster cluster = clusterMapper.selectById(id);
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            return client.pods().inNamespace(namespace)
                    .withName(podName)
                    .inContainer(containerName)
                    .getLog();
        }
    }
}
