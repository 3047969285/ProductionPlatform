package com.devflow.common;

import org.slf4j.MDC;

/**
 * 日志上下文工具
 */
public final class LogContext {

    public static final String TRACE_ID = "traceId";

    /**
     * 私有构造防止实例化
     */
    private LogContext() {}

    /**
     * 设置链路追踪编号
     *
     * @param traceId 追踪编号
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId); // 写入 SLF4J 上下文，日志 pattern 可自动打印
    }

    /**
     * 获取当前链路追踪编号
     *
     * @return 追踪编号
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 清理日志上下文
     */
    public static void clear() {
        MDC.remove(TRACE_ID); // 请求结束必须清理，避免线程池复用污染
    }
}
