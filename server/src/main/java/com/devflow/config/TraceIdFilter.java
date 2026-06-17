package com.devflow.config;

import com.devflow.common.LogContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求链路追踪过滤器
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 尽量早执行，后续日志都能带上 traceId
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 为每个请求生成或透传 traceId
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 优先用前端传来的 traceId，方便跨服务串联
        String traceId = request.getHeader(HEADER_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        LogContext.setTraceId(traceId);
        response.setHeader(HEADER_TRACE_ID, traceId); // 回写给调用方
        try {
            filterChain.doFilter(request, response);
        } finally {
            LogContext.clear(); // 无论成功失败都清理，防止线程复用串号
        }
    }
}
