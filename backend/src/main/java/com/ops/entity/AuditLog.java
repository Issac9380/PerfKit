package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体类
 * 对应数据库中的audit_log表，记录用户操作行为
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("audit_log")
public class AuditLog {
    /**
     * 日志ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     * 执行该操作的用户ID
     */
    private Long userId;

    /**
     * 操作类型
     * 操作的名称，如 LOGIN、CREATE_CLUSTER、EXECUTE_COMMAND 等
     */
    private String action;

    /**
     * 资源类型
     * 被操作资源的类型，如 CLUSTER、POD、USER 等
     */
    private String resourceType;

    /**
     * 资源ID
     * 被操作资源的唯一标识
     */
    private Long resourceId;

    /**
     * 请求参数
     * 操作时的请求参数JSON字符串
     */
    private String requestParams;

    /**
     * IP地址
     * 发起请求的客户端IP地址
     */
    private String ipAddress;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
