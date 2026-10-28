package com.zzg.psychologyai.infrastructure.security;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.zzg.psychologyai.application.dto.AuthenticatedUser;
import com.zzg.psychologyai.domain.port.TokenProvider;
import com.zzg.psychologyai.infrastructure.redis.RedisKeys;
import com.zzg.psychologyai.infrastructure.security.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JWT 令牌工具（基础设施层）：实现应用层令牌签发端口。
 * <p>
 * 登出与刷新基于 Redis 黑名单：登出时把 token 剩余有效期写入黑名单 key，
 * 校验时若命中黑名单则视为无效；刷新时校验旧 token 后签发新 token，旧 token 同时拉黑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_JWT_ID = "jti";

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String createToken(AuthenticatedUser user) {
        long nowSeconds = DateUtil.currentSeconds();
        Map<String, Object> payload = new HashMap<>(8);
        payload.put(CLAIM_JWT_ID, UUID.randomUUID().toString());
        payload.put(CLAIM_USER_ID, user.getUserId());
        payload.put(CLAIM_USERNAME, user.getUsername());
        payload.put(CLAIM_USER_TYPE, user.getUserType());
        payload.put(JWTPayload.ISSUED_AT, nowSeconds);
        payload.put(JWTPayload.EXPIRES_AT, nowSeconds + jwtProperties.getExpireSeconds());
        return JWTUtil.createToken(payload, secretKey());
    }

    @Override
    public long getExpireSeconds() {
        return jwtProperties.getExpireSeconds();
    }

    @Override
    public void invalidate(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }
        resolveRemainingSeconds(token).ifPresent(ttl -> {
            stringRedisTemplate.opsForValue().set(RedisKeys.authBlacklist(token), "logout", Duration.ofSeconds(ttl));
            log.info("token 已加入黑名单, ttl={}s", ttl);
        });
    }

    @Override
    public String refresh(String oldToken) {
        JWT jwt = parseToken(oldToken)
                .orElseThrow(() -> new IllegalArgumentException("令牌无效或已过期"));
        // 旧 token 作废
        invalidate(oldToken);
        // 用旧 token 载荷重新签发
        AuthenticatedUser user = new AuthenticatedUser(
                Convert.toLong(jwt.getPayload(CLAIM_USER_ID)),
                Convert.toStr(jwt.getPayload(CLAIM_USERNAME)),
                Convert.toInt(jwt.getPayload(CLAIM_USER_TYPE))
        );
        return createToken(user);
    }

    /**
     * 校验并解析令牌（签名错误、格式非法、已过期、已拉黑均返回空）
     */
    public Optional<JWT> parseToken(String token) {
        if (StrUtil.isBlank(token)) {
            return Optional.empty();
        }
        try {
            if (!JWTUtil.verify(token, secretKey())) {
                return Optional.empty();
            }
            JWT jwt = JWTUtil.parseToken(token);
            Long expireSeconds = Convert.toLong(jwt.getPayload(JWTPayload.EXPIRES_AT));
            if (expireSeconds == null || DateUtil.currentSeconds() > expireSeconds) {
                return Optional.empty();
            }
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisKeys.authBlacklist(token)))) {
                log.debug("token 命中黑名单");
                return Optional.empty();
            }
            return Optional.of(jwt);
        } catch (Exception e) {
            log.error("token 解析失败, reason={}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 从令牌载荷还原登录用户主体
     */
    public LoginUser toLoginUser(JWT jwt) {
        return new LoginUser(Convert.toLong(jwt.getPayload(CLAIM_USER_ID)),
                Convert.toStr(jwt.getPayload(CLAIM_USERNAME)),
                Convert.toInt(jwt.getPayload(CLAIM_USER_TYPE)));
    }

    @Override
    public AuthenticatedUser parse(String token) {
        return parseToken(token)
                .map(jwt -> new AuthenticatedUser(
                        Convert.toLong(jwt.getPayload(CLAIM_USER_ID)),
                        Convert.toStr(jwt.getPayload(CLAIM_USERNAME)),
                        Convert.toInt(jwt.getPayload(CLAIM_USER_TYPE))
                ))
                .orElse(null);
    }

    /**
     * 计算 token 剩余有效期（秒），已过期返回空
     */
    private Optional<Long> resolveRemainingSeconds(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Long expireAt = Convert.toLong(jwt.getPayload(JWTPayload.EXPIRES_AT));
            if (expireAt == null) {
                return Optional.empty();
            }
            long remaining = expireAt - DateUtil.currentSeconds();
            return remaining > 0 ? Optional.of(remaining) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private byte[] secretKey() {
        return jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    }
}
