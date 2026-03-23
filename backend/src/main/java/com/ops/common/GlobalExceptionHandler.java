package com.ops.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理系统中的异常，并返回标准化的错误响应
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 捕获BusinessException并返回对应的错误信息
     *
     * @param e 业务异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     * 捕获所有未处理的异常，返回统一的服务器错误响应
     *
     * @param e 异常对象
     * @return 错误响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统内部错误");
    }
}
