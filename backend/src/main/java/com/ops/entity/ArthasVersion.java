package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Arthas版本实体类
 * 对应数据库中的arthas_version表，存储Arthas诊断工具版本信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("arthas_version")
public class ArthasVersion {
    /**
     * Arthas版本ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Arthas版本号
     * 如 3.7.2、3.7.1、3.6.8 等
     */
    private String version;

    /**
     * 文件路径
     * Arthas包在本地的存储路径
     */
    private String filePath;

    /**
     * 文件大小
     * Arthas安装包的字节大小
     */
    private Long fileSize;

    /**
     * MD5校验值
     * 用于验证文件完整性
     */
    private String md5;

    /**
     * 官方下载链接
     * Arthas的官方下载地址
     */
    private String downloadUrl;

    /**
     * 兼容的JDK版本范围
     * 格式示例: 8,11,17,21 表示支持JDK 8/11/17/21
     */
    private String compatibleJdkVersions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
