package com.zzg.psychologyai.application;

import com.zzg.psychologyai.application.command.LoginCommand;
import com.zzg.psychologyai.application.dto.LoginResult;
import com.zzg.psychologyai.application.dto.AuthenticatedUser;
import com.zzg.psychologyai.domain.port.CredentialVerifier;
import com.zzg.psychologyai.domain.port.TokenProvider;
import com.zzg.psychologyai.domain.port.DomainEventPublisher;
import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.domain.model.User;
import com.zzg.psychologyai.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 认证命令服务实现（写侧）：登录 / 登出 / 刷新令牌编排。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthAppService {

    private final CredentialVerifier credentialVerifier;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 用户登录
     *
     * @param command 登录命令
     * @return 登录结果（令牌 + 有效期 + 用户信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(LoginCommand command) {
        AuthenticatedUser authenticated = credentialVerifier.verify(command.username(), command.password());

        User user = userRepository.findByUsername(authenticated.getUsername())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED, "用户不存在或已注销"));

        user.markLoggedIn();
        domainEventPublisher.publishEvents(user);
        String token = tokenProvider.createToken(authenticated);
        UserView userInfo = UserView.from(user);
        log.info("登录成功, userId={}, username={}", userInfo.id(), userInfo.username());
        return new LoginResult(token, tokenProvider.getExpireSeconds(), userInfo);
    }

    /**
     * 登出：将当前令牌加入黑名单
     *
     * @param token 当前请求携带的令牌
     */
    public void logout(String token) {
        tokenProvider.invalidate(token);
        log.info("用户已登出");
    }


    /**
     * 刷新令牌：校验旧令牌后签发新令牌，旧令牌同时作废
     *
     * @param oldToken 旧令牌
     * @return 新登录结果（新令牌 + 用户信息）
     */
    @Transactional(readOnly = true)
    public LoginResult refresh(String oldToken) {
        // 先解析旧 token 拿用户名（此时还未拉黑），再签发新 token（内部作废旧 token）
        AuthenticatedUser user = tokenProvider.parse(oldToken);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "令牌无效或已过期");
        }
        String newToken = tokenProvider.refresh(oldToken);
        User newUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED, "用户不存在或已注销"));
        UserView userInfo = UserView.from(newUser);
        log.info("令牌刷新成功, userId={}", userInfo.id());
        return new LoginResult(newToken, tokenProvider.getExpireSeconds(), userInfo);
    }


}
