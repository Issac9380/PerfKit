package com.ops.dto;

import lombok.Data;

/**
 * 日志分析请求DTO
 * 日志分析接口的请求参数封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class LogAnalyzeRequest {
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
     * 目标Pod的名称（支持模糊匹配）
     */
    private String podName;

    /**
     * 容器名称
     * 目标容器的名称
     */
    private String containerName;

    /**
     * 容器类型
     * 容器类型，如 springboot、tomcat、nginx等
     * 用于确定日志路径
     */
    private String containerType;

    /**
     * 日志行数
     * 每次获取的日志尾部行数，默认500行
     */
    private Integer logLines = 500;

    /**
     * 是否使用AI分析
     * true: 使用AI进行日志分析
     * false: 仅返回日志内容，不进行AI分析
     */
    private boolean useAI = false;

    /**
     * 使用的AI模型
     * 指定使用的AI模型提供商，如 claude、openai、azure等
     */
    private String aiModel = "claude";
}
