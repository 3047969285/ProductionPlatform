package com.devflow.common.exception;

import com.devflow.common.ResultCode;

/**
 * 业务断言工具
 */
public final class BizAssert {

    /**
     * 私有构造防止实例化
     */
    private BizAssert() {}

    /**
     * 断言字符串非空
     *
     * @param value 待校验字符串
     * @param message 错误信息
     */
    public static void notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, message); // 参数类错误返回 400
        }
    }

    /**
     * 断言对象非空
     *
     * @param value 待校验对象
     * @param message 错误信息
     */
    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
    }

    /**
     * 断言条件为真
     *
     * @param expression 条件表达式
     * @param message 错误信息
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BizException(message); // 默认业务错误码
        }
    }

    /**
     * 断言资源存在
     *
     * @param value 资源对象
     * @param message 错误信息
     */
    public static void notNullResource(Object value, String message) {
        if (value == null) {
            throw new BizException(ResultCode.NOT_FOUND, message); // 资源不存在返回 404
        }
    }
}
