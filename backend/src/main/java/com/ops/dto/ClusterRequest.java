package com.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 集群请求DTO
 * 创建/更新K8S集群接口的请求参数封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class ClusterRequest {
    /**
     * 集群名称
     * 用于标识和显示的名称，必填项
     */
    @NotBlank(message = "集群名称不能为空")
    private String name;

    /**
     * API Server地址
     * Kubernetes集群的API Server访问地址，必填项
     */
    @NotBlank(message = "API Server 地址不能为空")
    private String apiServer;

    /**
     * 认证类型
     * 支持 kubeconfig、token、certificate 三种方式，必填项
     */
    @NotBlank(message = "认证类型不能为空")
    private String authType;

    /**
     * 认证配置
     * 存储kubeconfig内容或token等认证信息
     */
    private String config;

    /**
     * 集群描述
     * 用于描述集群的用途、特性等信息
     */
    private String description;
}
