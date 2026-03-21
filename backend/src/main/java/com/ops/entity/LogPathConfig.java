package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("log_path_config")
public class LogPathConfig {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long clusterId;
    private String containerType;
    private String logPathPattern;
    private String description;
    private LocalDateTime createdAt;
}
