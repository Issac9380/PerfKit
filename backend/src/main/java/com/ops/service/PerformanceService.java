package com.ops.service;

import com.ops.dto.ExecuteCommandRequest;
import com.ops.dto.ExecuteCommandResult;

/**
 * 性能分析服务接口
 * 定义性能分析和命令执行相关的业务逻辑，包括在K8s环境中执行性能诊断命令、
 * 获取性能指标数据等功能
 *
 * @author Issac Song
 * @date 2026-03-23
 */
public interface PerformanceService {
    /**
     * 执行性能诊断命令
     * 在指定的Kubernetes容器中执行性能分析命令，返回命令执行结果
     *
     * @param request 命令执行请求对象，包含目标集群、命名空间、Pod、容器信息以及要执行的命令
     * @return 返回ExecuteCommandResult对象，包含命令执行输出、状态码等信息
     */
    ExecuteCommandResult execute(ExecuteCommandRequest request);
}
