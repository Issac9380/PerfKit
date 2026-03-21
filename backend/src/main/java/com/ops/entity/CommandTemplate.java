package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("command_template")
public class CommandTemplate {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String commandType;
    private String template;
    private String description;
    private LocalDateTime createdAt;
}
