package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * JDK版本实体类
 * 对应数据库中的jdk_version表，存储JDK版本管理信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("jdk_version")
public class JdkVersion {
    /**
     * JDK版本ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * JDK版本号
     * 如 8、11、17、21 等
     */
    private String version;

    /**
     * 文件路径
     * JDK安装包在本地的存储路径
     */
    private String filePath;

    /**
     * 文件大小
     * JDK安装包的字节大小
     */
    private Long fileSize;

    /**
     * MD5校验值
     * 用于验证文件完整性
     */
    private String md5;

    /**
     * 官方下载链接
     * JDK的官方下载地址
     */
    private String downloadUrl;

    /**
     * 推荐的Arthas版本
     * 与此JDK版本配套使用的Arthas版本
     */
    private String recommendedArthasVersion;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
