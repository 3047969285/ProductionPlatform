package com.devflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证相关配置项。
 */
@Data
@ConfigurationProperties(prefix = "devflow.auth")
public class AuthProperties {

    /** 访问令牌有效期（小时），默认 24 小时。 */
    private int tokenTtlHours = 24;
}
