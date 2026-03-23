package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志路径配置实体类
 * 对应数据库中的log_path_config表，存储容器日志路径配置
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("log_path_config")
public class LogPathConfig {
    /**
     * 配置ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 集群ID
     * 关联的K8S集群ID
     */
    private Long clusterId;

    /**
     * 容器类型
     * 如 springboot、tomcat、nginx等
     */
    private String containerType;

    /**
     * 日志路径模式
     * 容器内日志文件的路径模式，支持变量替换
     * 如 /var/log/{app}/app.log
     */
    private String logPathPattern;

    /**
     * 描述说明
     * 配置的用途和说明
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
