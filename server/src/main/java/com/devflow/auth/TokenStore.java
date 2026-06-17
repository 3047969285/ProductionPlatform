package com.devflow.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存令牌存储
 */
@Component
public class TokenStore {

    // key=令牌字符串，value=用户 ID
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    /**
     * 为用户创建访问令牌
     *
     * @param userId 用户编号
     * @return 新生成的令牌字符串
     */
    public String create(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", ""); // 去掉横线，32 位纯字符
        tokens.put(token, userId);
        return token;
    }

    /**
     * 根据令牌解析用户编号
     *
     * @param token 访问令牌
     * @return 用户编号无效令牌返回空
     */
    public Long getUserId(String token) {
        return token == null ? null : tokens.get(token);
    }

    /**
     * 移除令牌使其失效
     *
     * @param token 访问令牌
     */
    public void remove(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
