package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("arthas_version")
public class ArthasVersion {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String version;
    private String filePath;
    private Long fileSize;
    private String md5;
    private LocalDateTime createdAt;
}
