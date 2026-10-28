package com.zzg.psychologyai.infrastructure.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项（基础设施层）：密钥、有效期、请求头名称等，支持环境变量覆盖
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 签名密钥，长度建议不低于 32 字节；生产环境必须通过环境变量 JWT_SECRET 覆盖
     */
    private String secret;

    /**
     * 令牌有效期（秒），默认 2 小时
     */
    private long expireSeconds = 7200L;

    /**
     * 携带令牌的请求头名称
     */
    private String header = "Authorization";

    /**
     * 令牌前缀，解析时会截掉该前缀
     */
    private String prefix = "Bearer ";
}
