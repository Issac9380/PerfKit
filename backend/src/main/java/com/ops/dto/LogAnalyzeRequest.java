package com.ops.dto;

import lombok.Data;

@Data
public class LogAnalyzeRequest {
    private Long clusterId;
    private String namespace;
    private String podName;
    private String containerName;
    private String containerType;
    private Integer logLines = 500;
    private boolean useAI = false;
    private String aiModel = "claude";
}
