package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("case_library")
public class CaseLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String problemPattern;
    private String solution;
    private String containerType;
    private String tags;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
