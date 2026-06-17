package com.devflow.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应结构
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;
    private String traceId;

    /**
     * 按状态码构造响应
     *
     * @param code 状态码
     * @param message 提示信息
     * @param data 业务数据
     * @return 统一响应
     */
    public static <T> ApiResult<T> of(int code, String message, T data) {
        ApiResult<T> result = new ApiResult<>();
        result.code = code;
        result.message = message;
        result.data = data;
        result.timestamp = System.currentTimeMillis(); // 响应时间戳，方便排查
        result.traceId = LogContext.getTraceId(); // 关联本次请求的 traceId
        return result;
    }

    /**
     * 按状态码构造无数据响应
     *
     * @param code 状态码
     * @param message 提示信息
     * @return 统一响应
     */
    public static ApiResult<Void> of(int code, String message) {
        return of(code, message, null);
    }

    /**
     * 成功响应并携带数据
     *
     * @param data 业务数据
     * @return 成功响应
     */
    public static <T> ApiResult<T> ok(T data) {
        return of(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应无数据
     *
     * @return 成功响应
     */
    public static ApiResult<Void> ok() {
        return ok(null);
    }

    /**
     * 业务失败响应
     *
     * @param msg 错误信息
     * @return 失败响应
     */
    public static <T> ApiResult<T> fail(String msg) {
        return of(ResultCode.BUSINESS_ERROR.getCode(), msg, null);
    }

    /**
     * 请求参数错误响应
     *
     * @param msg 错误信息
     * @return 参数错误响应
     */
    public static <T> ApiResult<T> bad(String msg) {
        return of(ResultCode.BAD_REQUEST.getCode(), msg, null);
    }

    /**
     * 未授权响应
     *
     * @param msg 错误信息
     * @return 未授权响应
     */
    public static <T> ApiResult<T> unauthorized(String msg) {
        return of(ResultCode.UNAUTHORIZED.getCode(), msg, null);
    }
}
