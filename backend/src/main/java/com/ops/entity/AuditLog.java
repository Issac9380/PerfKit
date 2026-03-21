package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String requestParams;
    private String ipAddress;
    private LocalDateTime createdAt;
}
