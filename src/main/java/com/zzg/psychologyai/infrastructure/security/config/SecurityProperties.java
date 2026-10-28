package com.zzg.psychologyai.infrastructure.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全相关配置（基础设施层）：免认证白名单可在 application.yml 的 security.whitelist 中维护，
 * 后续新增免登录接口无需改动 Java 代码
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 免认证白名单路径（支持 Ant 风格，如 /user/**），默认登录、注册与错误转发接口
     */
    private List<String> whitelist = List.of("/user/login", "/user/add", "/error", "/");
}
