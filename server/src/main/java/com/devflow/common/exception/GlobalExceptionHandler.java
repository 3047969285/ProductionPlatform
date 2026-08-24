package com.devflow.common.exception;

import com.devflow.common.ApiResult;
import com.devflow.common.LogContext;
import com.devflow.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBizException(BizException e, HttpServletRequest request) {
        logWarn(request, e.getCode(), e.getMessage(), e);
        // 业务异常直接透传 code 和 message 给前端
        return ApiResult.of(e.getCode(), e.getMessage());
    }

    /**
     * 处理单参数校验异常
     *
     * @param e 约束违反异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        // @RequestParam 等单参数校验失败时触发，取第一条错误信息
        String message = e.getConstraintViolations().iterator().next().getMessage();
        logWarn(request, ResultCode.BAD_REQUEST.getCode(), message, e);
        return ApiResult.of(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理参数校验异常
     *
     * @param e 校验异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResult<Void> handleValidException(Exception e, HttpServletRequest request) {
        String message = extractValidMessage(e);
        logWarn(request, ResultCode.BAD_REQUEST.getCode(), message, e);
        return ApiResult.of(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e 参数缺失异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "缺少请求参数 " + e.getParameterName();
        logWarn(request, ResultCode.BAD_REQUEST.getCode(), message, e);
        return ApiResult.of(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理数据完整性约束异常
     *
     * @param e 数据完整性异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<Void> handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
        String message = "存在关联数据无法删除，请先清理子项或文件夹内容";
        logWarn(request, ResultCode.BAD_REQUEST.getCode(), message, e);
        return ApiResult.of(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理请求体解析异常
     *
     * @param e 解析异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Void> handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        logWarn(request, ResultCode.BAD_REQUEST.getCode(), "请求体格式错误", e);
        // 不暴露底层解析细节，统一提示格式错误
        return ApiResult.of(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 方法不支持异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        logWarn(request, ResultCode.METHOD_NOT_ALLOWED.getCode(), e.getMessage(), e);
        return ApiResult.of(ResultCode.METHOD_NOT_ALLOWED.getCode(), "请求方法不支持");
    }

    /**
     * 处理资源不存在异常
     *
     * @param e 404 异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResult<Void> handleNotFound(NoHandlerFoundException e, HttpServletRequest request) {
        logWarn(request, ResultCode.NOT_FOUND.getCode(), e.getMessage(), e);
        return ApiResult.of(ResultCode.NOT_FOUND.getCode(), "接口不存在");
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        logWarn(request, ResultCode.BAD_REQUEST.getCode(), e.getMessage(), e);
        return ApiResult.of(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 处理未知系统异常
     *
     * @param e 系统异常
     * @param request HTTP 请求
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e, HttpServletRequest request) {
        logError(request, ResultCode.SYSTEM_ERROR.getCode(), e.getMessage(), e);
        // 系统异常对外隐藏细节，只返回通用提示
        return ApiResult.of(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage());
    }

    /**
     * 从校验异常中提取第一条字段错误信息
     *
     * @param e 校验异常
     * @return 错误提示文本
     */
    private String extractValidMessage(Exception e) {
        // @RequestBody + @Valid 校验失败
        if (e instanceof MethodArgumentNotValidException ex && ex.getBindingResult().hasFieldErrors()) {
            return ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        // 表单绑定校验失败
        if (e instanceof BindException ex && ex.getBindingResult().hasFieldErrors()) {
            return ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        return ResultCode.BAD_REQUEST.getMessage();
    }

    /**
     * 记录警告级别异常日志
     *
     * @param request HTTP 请求
     * @param code 响应状态码
     * @param message 错误信息
     * @param e 异常对象
     */
    private void logWarn(HttpServletRequest request, int code, String message, Exception e) {
        log.warn(buildLogMessage(request, code, message), e);
    }

    /**
     * 记录错误级别异常日志
     *
     * @param request HTTP 请求
     * @param code 响应状态码
     * @param message 错误信息
     * @param e 异常对象
     */
    private void logError(HttpServletRequest request, int code, String message, Exception e) {
        log.error(buildLogMessage(request, code, message), e);
    }

    /**
     * 拼装统一格式的异常日志
     *
     * @param request HTTP 请求
     * @param code 响应状态码
     * @param message 错误信息
     * @return 格式化日志文本
     */
    private String buildLogMessage(HttpServletRequest request, int code, String message) {
        return String.format(
                "[API异常] traceId=%s code=%d method=%s uri=%s query=%s ip=%s msg=%s",
                LogContext.getTraceId(),
                code,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr(),
                message
        );
    }
}
