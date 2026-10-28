package com.zzg.psychologyai.domain.port;

import com.zzg.psychologyai.application.dto.AuthenticatedUser;

/**
 * 凭证校验端口（应用层定义，基础设施层以 Spring Security 实现）：
 * 应用层只声明“校验用户名密码”的能力，不依赖具体认证框架
 */
public interface CredentialVerifier {

    /**
     * 校验用户名与密码
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 认证通过的用户信息
     * @throws com.zzg.psychologyai.common.exception.BizException 校验失败时抛出（不区分用户不存在与密码错误）
     */
    AuthenticatedUser verify(String username, String rawPassword);
}
