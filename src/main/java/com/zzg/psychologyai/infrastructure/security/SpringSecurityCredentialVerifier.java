package com.zzg.psychologyai.infrastructure.security;

import com.zzg.psychologyai.application.dto.AuthenticatedUser;
import com.zzg.psychologyai.domain.port.CredentialVerifier;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * 凭证校验端口适配器（基础设施层）：以 Spring Security 的 AuthenticationManager 实现，
 * 屏蔽认证框架细节，供应用层调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringSecurityCredentialVerifier implements CredentialVerifier {

    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticatedUser verify(String username, String rawPassword) {
        Authentication authentication;
        try {
            // 交由 Spring Security 完成用户名密码校验（UserDetailsServiceImpl + BCrypt）
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPassword));
        } catch (AuthenticationException e) {
            // 不区分“用户不存在”与“密码错误”，避免账号被枚举
            log.warn("凭证校验失败, username={}", username);
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return new AuthenticatedUser(loginUser.getUserId(), loginUser.getUsername(), loginUser.getUserType());
    }
}
