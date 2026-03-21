package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("jdk_version")
public class JdkVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String version;
    private String filePath;
    private Long fileSize;
    private String md5;
    private LocalDateTime createdAt;
}
