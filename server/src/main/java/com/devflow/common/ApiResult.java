package com.devflow.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) { return new ApiResult<>(200, "success", data); }
    public static ApiResult<Void> ok() { return ok(null); }
    public static <T> ApiResult<T> fail(String msg) { return new ApiResult<>(500, msg, null); }
    public static <T> ApiResult<T> bad(String msg) { return new ApiResult<>(400, msg, null); }
    public static <T> ApiResult<T> unauthorized(String msg) { return new ApiResult<>(401, msg, null); }
}
