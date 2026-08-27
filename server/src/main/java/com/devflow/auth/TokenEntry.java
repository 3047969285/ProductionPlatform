package com.devflow.auth;

/**
 * 内存令牌条目，记录绑定的用户与过期时间。
 *
 * @param userId       用户编号
 * @param expiresAtMs  过期时间戳（毫秒）
 */
record TokenEntry(long userId, long expiresAtMs) {
}
