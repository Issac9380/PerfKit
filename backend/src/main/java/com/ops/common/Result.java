package com.ops.common;

import lombok.Data;

/**
 * 统一返回结果封装类
 * 用于所有API接口的标准化响应格式
 *
 * @author Issac Song
 * @date 2026-03-23
 */
@Data
public class Result<T> {
    /**
     * 响应状态码
     * 200: 成功
     * 400: 请求参数错误
     * 401: 未授权
     * 403: 禁止访问
     * 404: 资源不存在
     * 500: 服务器内部错误
     */
    private int code;

    /**
     * 响应消息
     * 用于返回操作结果描述信息
     */
    private String message;

    /**
     * 响应数据
     * 泛型支持，返回任意类型的数据
     */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 返回的数据对象
     * @param <T>  泛型类型
     * @return 封装好的Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     * 用于不需要返回数据的操作，如删除、修改等
     *
     * @param <T> 泛型类型
     * @return 封装好的Result对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 错误响应（自定义状态码）
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     泛型类型
     * @return 封装好的Result对象
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 错误响应（默认500状态码）
     *
     * @param message 错误消息
     * @param <T>     泛型类型
     * @return 封装好的Result对象
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
