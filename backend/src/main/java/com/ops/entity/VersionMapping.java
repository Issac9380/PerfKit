package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * JDK/Arthas版本映射实体类
 * 对应数据库中的version_mapping表，存储JDK与Arthas版本的对应关系
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("version_mapping")
public class VersionMapping {

    /**
     * 映射ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * JDK版本
     * 如 8、11、17、21
     */
    private String jdkVersion;

    /**
     * Arthas版本
     * 如 3.7.2、3.7.1、3.6.8
     */
    private String arthasVersion;

    /**
     * 描述说明
     * 描述此版本组合的特点和用途
     */
    private String description;

    /**
     * 是否推荐使用
     * true: 推荐使用此版本组合
     * false: 可选版本组合
     */
    private Boolean recommended;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
