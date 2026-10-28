package com.zzg.psychologyai.domain.port;

import com.zzg.psychologyai.application.dto.AuthenticatedUser;

/**
 * 令牌签发端口（应用层定义，基础设施层以 JWT + Redis 黑名单实现）
 */
public interface TokenProvider {

    String createToken(AuthenticatedUser user);

    long getExpireSeconds();

    /**
     * 登出：将令牌加入黑名单
     */
    void invalidate(String token);

    /**
     * 刷新令牌：校验旧令牌后签发新令牌，旧令牌同时作废
     */
    String refresh(String oldToken);

    /**
     * 解析令牌（校验签名、过期、黑名单），返回认证用户信息
     *
     * @param token 令牌
     * @return 认证用户，无效时返回 null
     */
    AuthenticatedUser parse(String token);
}
