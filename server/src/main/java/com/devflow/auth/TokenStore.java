package com.devflow.auth;

import com.devflow.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存令牌存储，支持过期时间与登出失效。
 */
@Component
@RequiredArgsConstructor
public class TokenStore {

    private final AuthProperties authProperties;

    /** key=令牌字符串，value=令牌条目（用户编号 + 过期时间）。 */
    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();

    /**
     * 为用户创建访问令牌。
     *
     * @param userId 用户编号
     * @return 新生成的令牌字符串
     */
    public String create(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        long expiresAtMs = System.currentTimeMillis() + resolveTtlMillis();
        tokens.put(token, new TokenEntry(userId, expiresAtMs));
        return token;
    }

    /**
     * 根据令牌解析用户编号，过期令牌会自动移除。
     *
     * @param token 访问令牌
     * @return 用户编号；无效或过期令牌返回 null
     */
    public Long getUserId(String token) {
        if (token == null) {
            return null;
        }
        TokenEntry entry = tokens.get(token);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expiresAtMs()) {
            tokens.remove(token);
            return null;
        }
        return entry.userId();
    }

    /**
     * 移除令牌使其失效。
     *
     * @param token 访问令牌
     */
    public void remove(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    /**
     * 解析配置中的令牌有效期（毫秒）。
     *
     * @return TTL 毫秒数，至少 1 小时
     */
    private long resolveTtlMillis() {
        int hours = Math.max(authProperties.getTokenTtlHours(), 1);
        return Duration.ofHours(hours).toMillis();
    }
}
