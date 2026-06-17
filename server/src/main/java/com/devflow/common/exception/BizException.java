package com.devflow.common.exception;

import com.devflow.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    /**
     * 使用默认业务错误码
     *
     * @param message 错误信息
     */
    public BizException(String message) {
        this(ResultCode.BUSINESS_ERROR.getCode(), message); // 默认 500 业务错误
    }

    /**
     * 指定错误码
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code; // 会随异常一起返回给前端
    }

    /**
     * 使用枚举状态码
     *
     * @param resultCode 状态枚举
     */
    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 使用枚举状态码并自定义信息
     *
     * @param resultCode 状态枚举
     * @param message 自定义错误信息
     */
    public BizException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message); // 用枚举的 code，但 message 可覆盖
    }
}
