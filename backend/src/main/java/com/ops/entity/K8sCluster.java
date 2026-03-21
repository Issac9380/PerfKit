package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("k8s_cluster")
public class K8sCluster {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String apiServer;
    private String authType;
    private String config;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String status;
}
