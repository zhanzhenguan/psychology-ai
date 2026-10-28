package com.zzg.psychologyai.infrastructure.security.password;

import com.zzg.psychologyai.domain.service.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码编码端口适配器（基础设施层）：以 Spring Security 的 BCrypt 实现领域端口，
 * 使应用/领域层不直接依赖 Spring Security
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    /**
     * 委托给 Spring Security 的 BCrypt 实现，保证与认证框架校验一致
     */
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
