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

/**
 * K8sServiceImpl 实现类
 * 实现Kubernetes集群管理的业务逻辑，包括集群的增删改查、连接测试、命名空间/Pod/容器查询、日志获取等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class K8sServiceImpl implements K8sService {

    /**
     * K8sClusterMapper数据库操作类
     * 用于集群信息的持久化操作
     */
    private final K8sClusterMapper clusterMapper;

    /**
     * KubernetesClientFactory工厂类
     * 用于创建和管理Kubernetes客户端连接
     */
    private final KubernetesClientFactory clientFactory;

    /**
     * 获取所有Kubernetes集群列表
     *
     * @return 集群实体列表
     */
    @Override
    public List<K8sCluster> list() {
        return clusterMapper.selectList(null);
    }

    /**
     * 创建新的Kubernetes集群
     * 根据请求参数创建集群配置并保存到数据库
     *
     * @param request 集群创建请求参数，包含名称、API Server地址、认证方式、配置等信息
     * @return 创建成功的集群实体对象
     */
    @Override
    public K8sCluster create(ClusterRequest request) {
        // 创建集群实体对象
        K8sCluster cluster = new K8sCluster();
        // 设置集群基本信息
        cluster.setName(request.getName());
        cluster.setApiServer(request.getApiServer());
        cluster.setAuthType(request.getAuthType());
        cluster.setConfig(request.getConfig());
        cluster.setDescription(request.getDescription());
        // 设置默认状态为活跃
        cluster.setStatus("ACTIVE");
        // 插入数据库
        clusterMapper.insert(cluster);
        return cluster;
    }

    /**
     * 更新Kubernetes集群配置
     * 根据ID查找集群并更新其配置信息
     *
     * @param id 集群ID
     * @param request 集群更新请求参数
     * @return 更新后的集群实体对象
     * @throws BusinessException 集群不存在时抛出404异常
     */
    @Override
    public K8sCluster update(Long id, ClusterRequest request) {
        // 根据ID查询集群
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        // 更新集群配置
        cluster.setName(request.getName());
        cluster.setApiServer(request.getApiServer());
        cluster.setAuthType(request.getAuthType());
        cluster.setConfig(request.getConfig());
        cluster.setDescription(request.getDescription());
        // 持久化更新
        clusterMapper.updateById(cluster);
        return cluster;
    }

    /**
     * 删除Kubernetes集群
     * 根据ID删除集群配置，同时关闭相关客户端连接
     *
     * @param id 集群ID
     */
    @Override
    public void delete(Long id) {
        // 删除集群记录
        clusterMapper.deleteById(id);
        // 关闭该集群的客户端连接，释放资源
        clientFactory.closeClient(id);
    }

    /**
     * 测试Kubernetes集群连接
     * 通过尝试连接集群的API Server来验证连接是否可用
     *
     * @param id 集群ID
     * @return 连接成功返回true，失败返回false
     * @throws BusinessException 集群不存在时抛出404异常
     */
    @Override
    public boolean testConnection(Long id) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            // 尝试获取命名空间列表来测试连接
            client.namespaces().list();
            return true;
        } catch (Exception e) {
            log.error("K8S connection test failed", e);
            return false;
        }
    }

    /**
     * 获取集群下的所有命名空间
     *
     * @param id 集群ID
     * @return 命名空间名称列表
     */
    @Override
    public List<String> getNamespaces(Long id) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            // 获取命名空间列表并提取名称
            return client.namespaces().list().getItems().stream()
                    .map(ns -> ns.getMetadata().getName())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取命名空间下的所有Pod列表
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @return Pod名称列表
     */
    @Override
    public List<String> getPods(Long id, String namespace) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            // 获取指定命名空间下的Pod列表
            List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
            return pods.stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取Pod下的所有容器列表
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @param podName Pod名称
     * @return 容器名称列表
     */
    @Override
    public List<String> getContainers(Long id, String namespace, String podName) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            // 获取Pod详情并提取容器名称
            Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
            if (pod == null) {
                throw new BusinessException(404, "Pod不存在: " + podName);
            }
            return pod.getSpec().getContainers().stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取指定容器的日志
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @param podName Pod名称
     * @param containerName 容器名称
     * @return 容器日志内容
     */
    @Override
    public String getLogs(Long id, String namespace, String podName, String containerName) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {
            // 获取指定容器的日志
            return client.pods().inNamespace(namespace)
                    .withName(podName)
                    .inContainer(containerName)
                    .getLog();
        }
    }

    /**
     * 批量获取多个Pod的日志
     * 遍历指定的Pod列表，获取每个Pod第一个容器的日志
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @param podNames Pod名称列表
     * @return 格式化后的日志内容，包含各Pod的日志段落
     */
    @Override
    public String getBatchLogs(Long id, String namespace, List<String> podNames) {
        // 查询集群配置
        K8sCluster cluster = clusterMapper.selectById(id);
        if (cluster == null) {
            throw new BusinessException(404, "集群不存在");
        }
        // 用于存储汇总后的日志结果
        StringBuilder result = new StringBuilder();

        try (KubernetesClient client = clientFactory.getClient(
                cluster.getApiServer(), cluster.getAuthType(), cluster.getConfig())) {

            // 遍历每个Pod获取日志
            for (String podName : podNames) {
                // 添加Pod分隔标记
                result.append("========== Pod: ").append(podName).append(" ==========\n");
                try {
                    // 获取Pod信息
                    Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
                    if (pod == null) {
                        result.append("Pod not found\n");
                        continue;
                    }

                    // 获取Pod的第一个容器名称
                    List<String> containers = pod.getSpec().getContainers().stream()
                            .map(c -> c.getName())
                            .collect(Collectors.toList());

                    // 如果容器存在则获取日志
                    if (!containers.isEmpty()) {
                        String log = client.pods().inNamespace(namespace)
                                .withName(podName)
                                .inContainer(containers.get(0))
                                .getLog();
                        result.append(log).append("\n");
                    } else {
                        result.append("No containers found\n");
                    }
                } catch (Exception e) {
                    // 捕获获取日志时的异常
                    result.append("Error getting logs: ").append(e.getMessage()).append("\n");
                }
                result.append("\n");
            }
        }
        return result.toString();
    }
}
