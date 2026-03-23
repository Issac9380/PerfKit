package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * K8S集群实体类
 * 对应数据库中的k8s_cluster表，存储Kubernetes集群配置信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("k8s_cluster")
public class K8sCluster {
    /**
     * 集群ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 集群名称
     * 用于标识和显示的名称
     */
    private String name;

    /**
     * API Server地址
     * Kubernetes集群的API Server访问地址
     */
    private String apiServer;

    /**
     * 认证类型
     * 支持 kubeconfig、token、certificate 三种认证方式
     */
    private String authType;

    /**
     * 认证配置
     * 存储kubeconfig内容或token等认证信息（加密存储）
     */
    private String config;

    /**
     * 集群描述
     * 用于描述集群的用途、特性等信息
     */
    private String description;

    /**
     * 创建人ID
     * 记录创建该集群配置的用户ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 集群状态
     * ACTIVE: 活跃
     * INACTIVE: 未激活
     */
    private String status;
}
