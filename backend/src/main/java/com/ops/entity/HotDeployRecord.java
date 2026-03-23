package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热部署记录实体类
 * 对应数据库中的hot_deploy_record表，记录Arthas热部署操作
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("hot_deploy_record")
public class HotDeployRecord {
    /**
     * 记录ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 集群ID
     * 关联的K8S集群ID
     */
    private Long clusterId;

    /**
     * 命名空间
     * Pod所在的Kubernetes命名空间
     */
    private String namespace;

    /**
     * Pod名称
     * 目标Pod的名称
     */
    private String podName;

    /**
     * 容器名称
     * 目标容器的名称
     */
    private String containerName;

    /**
     * 文件名
     * 热部署的文件名
     */
    private String fileName;

    /**
     * 类名
     * 热部署的类名
     */
    private String className;

    /**
     * 方法名
     * 热部署的方法名
     */
    private String methodName;

    /**
     * 状态
     * 操作状态:
     * - PENDING: 待执行
     * - RUNNING: 执行中
     * - SUCCESS: 成功
     * - FAILED: 失败
     */
    private String status;

    /**
     * 创建人ID
     * 执行该操作的用户ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
