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
}
