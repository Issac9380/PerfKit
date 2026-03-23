package com.ops.service;

import com.ops.dto.ClusterRequest;
import com.ops.entity.K8sCluster;

import java.util.List;

/**
 * K8s集群管理服务接口
 * 定义Kubernetes集群管理相关的业务逻辑，包括集群的增删改查、连接测试、
 * 命名空间查询、Pod管理、日志获取等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface K8sService {
    /**
     * 获取所有K8s集群列表
     * 查询系统中所有已注册的Kubernetes集群信息
     *
     * @return 返回所有K8s集群对象的列表
     */
    List<K8sCluster> list();

    /**
     * 创建新的K8s集群
     * 根据提供的集群请求信息创建一个新的Kubernetes集群配置
     *
     * @param request 集群请求对象，包含集群的配置信息（如名称、API地址、认证信息等）
     * @return 返回创建的K8sCluster对象，包含完整的集群信息
     */
    K8sCluster create(ClusterRequest request);

    /**
     * 更新已有的K8s集群
     * 根据集群ID更新指定Kubernetes集群的配置信息
     *
     * @param id 要更新的集群ID
     * @param request 集群请求对象，包含新的配置信息
     * @return 返回更新后的K8sCluster对象
     */
    K8sCluster update(Long id, ClusterRequest request);

    /**
     * 删除指定的K8s集群
     * 根据集群ID删除指定的Kubernetes集群配置
     *
     * @param id 要删除的集群ID
     */
    void delete(Long id);

    /**
     * 测试K8s集群连接
     * 验证与指定Kubernetes集群的连接是否正常
     *
     * @param id 要测试连接的集群ID
     * @return 返回连接测试结果，true表示连接成功，false表示连接失败
     */
    boolean testConnection(Long id);

    /**
     * 获取集群下的所有命名空间
     * 查询指定Kubernetes集群中所有的命名空间
     *
     * @param id 集群ID
     * @return 返回该集群下所有命名空间的名称列表
     */
    List<String> getNamespaces(Long id);

    /**
     * 获取命名空间下的所有Pod
     * 查询指定命名空间中所有的Pod列表
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @return 返回该命名空间下所有Pod的名称列表
     */
    List<String> getPods(Long id, String namespace);

    /**
     * 获取Pod下的所有容器
     * 查询指定Pod中所有的容器名称
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @param podName Pod名称
     * @return 返回该Pod下所有容器的名称列表
     */
    List<String> getContainers(Long id, String namespace, String podName);

    /**
     * 获取指定容器的日志
     * 获取指定Kubernetes容器中的日志内容
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @param podName Pod名称
     * @param containerName 容器名称
     * @return 返回容器的日志内容字符串
     */
    String getLogs(Long id, String namespace, String podName, String containerName);

    /**
     * 批量获取多个Pod的日志
     * 一次性获取指定命名空间中多个Pod的日志内容
     *
     * @param id 集群ID
     * @param namespace 命名空间名称
     * @param podNames 要获取日志的Pod名称列表
     * @return 返回所有Pod的日志内容（可能已合并或分别返回）
     */
    String getBatchLogs(Long id, String namespace, List<String> podNames);
}
