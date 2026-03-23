package com.ops.service;

import com.ops.dto.LogAnalyzeRequest;
import com.ops.dto.LogAnalyzeResult;

/**
 * 日志分析服务接口
 * 定义日志分析相关的业务逻辑，包括日志内容的解析、统计、分析等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface LogAnalysisService {
    /**
     * 分析日志内容
     * 根据提供的日志分析请求对日志进行分析处理，返回分析结果
     *
     * @param request 日志分析请求对象，包含待分析的日志内容、分析参数等
     * @return 返回LogAnalyzeResult对象，包含分析结果、统计信息等
     */
    LogAnalyzeResult analyze(LogAnalyzeRequest request);
}
