package com.devflow.auth;

import com.devflow.config.AuthProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TokenStore} 单元测试。
 */
class TokenStoreTest {

    /**
     * 创建令牌后应能解析用户编号，登出后失效。
     */
    @Test
    void createResolveAndRemoveToken() {
        AuthProperties properties = new AuthProperties();
        properties.setTokenTtlHours(24);
        TokenStore tokenStore = new TokenStore(properties);

        String token = tokenStore.create(42L);

        assertThat(tokenStore.getUserId(token)).isEqualTo(42L);

        tokenStore.remove(token);

        assertThat(tokenStore.getUserId(token)).isNull();
    }
}
