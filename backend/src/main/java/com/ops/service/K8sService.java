package com.ops.service;

import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;

import java.util.List;

public interface K8sService {
    List<K8sCluster> list();
    K8sCluster create(ClusterRequest request);
    K8sCluster update(Long id, ClusterRequest request);
    void delete(Long id);
    boolean testConnection(Long id);
    List<String> getNamespaces(Long id);
    List<String> getPods(Long id, String namespace);
    List<String> getContainers(Long id, String namespace, String podName);
    String getLogs(Long id, String namespace, String podName, String containerName);
}
