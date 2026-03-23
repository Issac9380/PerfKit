package com.ops.dto;

import com.ops.entity.CaseLibrary;
import lombok.Data;

/**
 * 日志分析结果DTO
 * 日志分析接口的响应数据封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class LogAnalyzeResult {
    /**
     * 日志内容
     * 获取的原始日志内容
     */
    private String logContent;

    /**
     * 匹配的案例
     * 通过案例库匹配到的相似问题案例
     * 如果没有匹配，则为null
     */
    private CaseLibrary matchedCase;

    /**
     * AI分析结果
     * AI对日志的分析和诊断建议
     * 仅在useAI为true时有值
     */
    private String aiAnalysis;

    /**
     * 日志摘要
     * 日志内容的简要总结
     */
    private String summary;
}
