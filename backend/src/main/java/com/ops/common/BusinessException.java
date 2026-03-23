package com.ops.common;

import lombok.Getter;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常信息
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误状态码
     * 用于区分不同类型的业务异常
     */
    private final int code;

    /**
     * 构造业务异常（自定义状态码）
     *
     * @param code    错误状态码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（默认500状态码）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
