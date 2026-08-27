package com.devflow.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码编码与校验工具，新密码使用 BCrypt，并兼容历史 SHA-256 摘要。
 */
@Component
public class PasswordEncoder {

    /** BCrypt 哈希前缀标识（Spring Security 支持的几种变体）。 */
    private static final String BCRYPT_PREFIX_2A = "$2a$";
    private static final String BCRYPT_PREFIX_2B = "$2b$";
    private static final String BCRYPT_PREFIX_2Y = "$2y$";

    /** 历史 SHA-256 十六进制摘要长度。 */
    private static final int LEGACY_SHA256_HEX_LENGTH = 64;

    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    /**
     * 将明文密码编码为 BCrypt 哈希。
     *
     * @param plainPassword 明文密码
     * @return BCrypt 哈希字符串
     */
    public String encode(String plainPassword) {
        return bcryptEncoder.encode(plainPassword);
    }

    /**
     * 校验明文密码是否与库中存储的哈希匹配。
     * <p>优先使用 {@link BCryptPasswordEncoder#matches(CharSequence, String)} 校验 BCrypt；
     * 若存储值为历史 SHA-256 十六进制摘要，则回退到 {@link HashUtil#sha256(String)} 比对。</p>
     *
     * @param plainPassword 明文密码
     * @param storedHash    数据库中的哈希值
     * @return 匹配返回 true
     */
    public boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        if (isBcryptHash(storedHash)) {
            return bcryptEncoder.matches(plainPassword, storedHash);
        }
        if (isLegacySha256Hash(storedHash)) {
            return HashUtil.sha256(plainPassword).equals(storedHash);
        }
        return false;
    }

    /**
     * 判断存储的哈希是否需要在下次登录成功后升级为 BCrypt。
     *
     * @param storedHash 数据库中的哈希值
     * @return 非 BCrypt 格式时返回 true
     */
    public boolean needsUpgrade(String storedHash) {
        return storedHash != null && !isBcryptHash(storedHash);
    }

    /**
     * 判断是否为 BCrypt 哈希格式。
     *
     * @param storedHash 待检测字符串
     * @return BCrypt 格式返回 true
     */
    private boolean isBcryptHash(String storedHash) {
        return storedHash.startsWith(BCRYPT_PREFIX_2A)
                || storedHash.startsWith(BCRYPT_PREFIX_2B)
                || storedHash.startsWith(BCRYPT_PREFIX_2Y);
    }

    /**
     * 判断是否为历史 SHA-256 十六进制摘要（64 位小写 hex）。
     *
     * @param storedHash 待检测字符串
     * @return 符合 legacy 格式返回 true
     */
    private boolean isLegacySha256Hash(String storedHash) {
        if (storedHash.length() != LEGACY_SHA256_HEX_LENGTH) {
            return false;
        }
        for (int index = 0; index < storedHash.length(); index++) {
            char current = storedHash.charAt(index);
            boolean isHex = (current >= '0' && current <= '9')
                    || (current >= 'a' && current <= 'f')
                    || (current >= 'A' && current <= 'F');
            if (!isHex) {
                return false;
            }
        }
        return true;
    }
}
