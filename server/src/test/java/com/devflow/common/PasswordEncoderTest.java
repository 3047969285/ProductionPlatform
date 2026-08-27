package com.devflow.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PasswordEncoder} 单元测试。
 */
class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoder();

    /**
     * BCrypt 编码后应能正确匹配，且不需要升级。
     */
    @Test
    void encodesAndMatchesWithBcrypt() {
        String encoded = passwordEncoder.encode("secret123");

        assertThat(passwordEncoder.matches("secret123", encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrong", encoded)).isFalse();
        assertThat(passwordEncoder.needsUpgrade(encoded)).isFalse();
    }

    /**
     * 应兼容历史 SHA-256 十六进制摘要，并标记需要升级。
     */
    @Test
    void matchesLegacySha256Hash() {
        String legacyHash = HashUtil.sha256("admin123");

        assertThat(passwordEncoder.matches("admin123", legacyHash)).isTrue();
        assertThat(passwordEncoder.matches("wrong", legacyHash)).isFalse();
        assertThat(passwordEncoder.needsUpgrade(legacyHash)).isTrue();
    }
}
