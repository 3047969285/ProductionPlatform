package com.devflow.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块配置入口，启用 {@link AuthProperties} 绑定。
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfig {
}
