package com.ops.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExecuteCommandRequest {
    private Long clusterId;
    private String namespace;
    private String podName;
    private String containerName;
    private Long templateId;
    private Map<String, String> params;
}
