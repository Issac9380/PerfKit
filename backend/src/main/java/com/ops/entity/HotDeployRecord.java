package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hot_deploy_record")
public class HotDeployRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long clusterId;
    private String namespace;
    private String podName;
    private String containerName;
    private String fileName;
    private String className;
    private String methodName;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
}
