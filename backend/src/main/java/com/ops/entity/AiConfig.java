package com.ops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI大模型配置实体类
 * 对应数据库中的ai_config表，存储AI服务提供商配置信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
@TableName("ai_config")
public class AiConfig {

    /**
     * 配置ID
     * 使用雪花算法生成的唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置名称
     * 用于标识和显示AI配置
     */
    private String name;

    /**
     * AI服务提供商
     * 支持的提供商类型:
     * - openai: OpenAI GPT系列
     * - claude: Anthropic Claude系列
     * - azure: Azure OpenAI
     * - local: 本地部署的大模型
     */
    private String provider;

    /**
     * API密钥
     * 用于访问AI服务的认证密钥（加密存储）
     */
    private String apiKey;

    /**
     * API端点地址
     * 自定义API服务器地址，可用于代理或本地部署
     */
    private String endpoint;

    /**
     * 模型名称
     * 具体使用的AI模型，如 gpt-4o、claude-3-opus 等
     */
    private String model;

    /**
     * 是否默认配置
     * true: 作为默认AI配置使用
     * false: 非默认配置
     */
    private Boolean isDefault;

    /**
     * 配置状态
     * - ACTIVE: 激活使用中
     * - INACTIVE: 未激活
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
