package com.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClusterRequest {
    @NotBlank(message = "集群名称不能为空")
    private String name;
    @NotBlank(message = "API Server 地址不能为空")
    private String apiServer;
    @NotBlank(message = "认证类型不能为空")
    private String authType;
    private String config;
    private String description;
}
