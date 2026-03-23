package com.ops.dto;

import lombok.Data;

import java.util.Map;

/**
 * 执行命令请求DTO
 * 性能分析命令执行接口的请求参数封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class ExecuteCommandRequest {
    /**
     * 集群ID
     * 目标Pod所在的K8S集群ID
     */
    private Long clusterId;

    /**
     * 命名空间
     * 目标Pod所在的Kubernetes命名空间
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
     * 命令模板ID
     * 使用的命令模板ID
     * 如果指定了templateId，则使用模板中的命令
     */
    private Long templateId;

    /**
     * 命令参数字典
     * 用于替换命令模板中的变量
     * 键为变量名，值为替换值
     * 如 {"pid": "123", "className": "com.example.MyClass"}
     */
    private Map<String, String> params;
}
