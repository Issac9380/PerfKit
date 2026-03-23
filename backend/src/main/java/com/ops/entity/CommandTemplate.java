package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 命令模板实体类
 * 对应数据库中的command_template表，存储常用的诊断命令模板
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("command_template")
public class CommandTemplate {
    /**
     * 模板ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板名称
     * 命令模板的名称，如"JVM堆内存"、"JVM线程堆栈"等
     */
    private String name;

    /**
     * 命令类型
     * 模板的分类类型:
     * - jmap: JVM内存相关命令
     * - jstack: 线程堆栈相关命令
     * - arthas: Arthas诊断命令
     */
    private String commandType;

    /**
     * 命令模板
     * 支持变量替换的命令模板，如 jmap -heap {pid}
     * {pid}、{className}、{methodName}等为可替换变量
     */
    private String template;

    /**
     * 描述说明
     * 命令的用途说明和使用场景
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
