package com.devflow.auth;

import com.devflow.common.ApiResult;
import com.devflow.common.LogContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper;

    /**
     * 请求前置校验 Bearer 令牌
     *
     * @param req HTTP 请求
     * @param res HTTP 响应
     * @param handler 处理器
     * @return 令牌有效返回 true 否则写入 401 并返回 false
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        // 预检请求直接放行，由 CORS 处理
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        // 从 Authorization 头解析 Bearer 令牌
        String h = req.getHeader("Authorization");
        String token = h != null && h.startsWith("Bearer ") ? h.substring(7) : null;

        // 令牌无效则拦截，直接写 401 JSON 响应
        if (tokenStore.getUserId(token) == null) {
            log.warn("[API异常] traceId={} code=401 method={} uri={} query={} ip={} msg=未登录或令牌失效",
                    LogContext.getTraceId(), req.getMethod(), req.getRequestURI(), req.getQueryString(), req.getRemoteAddr());
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(res.getWriter(), ApiResult.unauthorized("请先登录"));
            return false; // 阻止进入 Controller
        }
        return true;
    }
}
