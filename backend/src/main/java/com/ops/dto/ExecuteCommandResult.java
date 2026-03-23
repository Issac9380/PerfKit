package com.ops.dto;

import lombok.Data;

/**
 * 执行命令结果DTO
 * 性能分析命令执行接口的响应数据封装
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class ExecuteCommandResult {
    /**
     * 实际执行的命令
     * 经过变量替换后实际执行的命令内容
     */
    private String command;

    /**
     * 执行是否成功
     * true: 命令执行成功
     * false: 命令执行失败
     */
    private boolean success;

    /**
     * 结果类型
     * 用于前端区分不同的展示方式:
     * - text: 普通文本输出（默认）
     * - log: 日志格式输出，带时间戳高亮
     * - json: JSON格式输出，带语法高亮
     * - table: 表格形式展示
     * - flamegraph: 火焰图数据
     * - file: 文件内容展示
     * - image: 图片展示（Base64编码）
     */
    private String resultType;

    /**
     * 命令输出
     * 命令执行的标准输出内容
     */
    private String output;

    /**
     * 错误信息
     * 命令执行失败时的错误信息
     * 执行成功时为空
     */
    private String error;

    /**
     * 额外数据
     * 用于传递特殊类型的额外数据，如火焰图数据、文件元信息等
     */
    private Object extraData;
}
