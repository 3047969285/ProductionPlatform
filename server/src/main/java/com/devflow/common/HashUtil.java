package com.devflow.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 哈希工具类
 */
public final class HashUtil {

    /**
     * 私有构造防止实例化
     */
    private HashUtil() {}

    /**
     * 计算字符串的 SHA256 十六进制摘要
     *
     * @param text 原始文本
     * @return 小写十六进制哈希值
     */
    public static String sha256(String text) {
        try {
            // 先算 SHA-256 字节数组，再转成十六进制字符串
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e); // 算法不存在属于 JVM 环境问题
        }
    }
}
