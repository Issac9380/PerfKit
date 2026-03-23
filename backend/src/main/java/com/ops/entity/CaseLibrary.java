package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案例库实体类
 * 对应数据库中的case_library表，存储问题案例和解决方案
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("case_library")
public class CaseLibrary {
    /**
     * 案例ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 案例标题
     * 问题的简要描述标题
     */
    private String title;

    /**
     * 问题模式
     * 问题的特征描述，用于匹配相似问题
     */
    private String problemPattern;

    /**
     * 解决方案
     * 针对该问题的解决方法描述
     */
    private String solution;

    /**
     * 容器类型
     * 适用的容器类型，如 springboot、dubbo、普通java等
     */
    private String containerType;

    /**
     * 标签
     * 用于分类和检索的标签，多个标签用逗号分隔
     */
    private String tags;

    /**
     * 创建人ID
     * 记录创建该案例的用户ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
