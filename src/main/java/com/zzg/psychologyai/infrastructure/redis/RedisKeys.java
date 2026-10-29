package com.zzg.psychologyai.infrastructure.redis;

/**
 * Redis key 公共常量类：统一管理所有 key 前缀与拼接规则，避免散落各处硬编码。
 * <p>
 * 命名规范：业务:模块:标识，多段用冒号分隔，便于按前缀批量扫描。
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /**
     * 认证模块：token 黑名单
     */
    public static final String AUTH_BLACKLIST = "auth:blacklist:";

    /**
     * 拼接 token 黑名单 key
     *
     * @param token JWT 字符串
     * @return 完整 key
     */
    public static String authBlacklist(String token) {
        return AUTH_BLACKLIST + token;
    }

    /**
     * 用户模块：用户信息缓存
     */
    public static final String USER_INFO = "user:info:";

    /**
     * 拼接用户信息缓存 key
     *
     * @param userId 用户ID
     * @return 完整 key
     */
    public static String userInfo(Long userId) {
        return USER_INFO + userId;
    }

    /**
     * 心理对话模块：流式接口限流（并发数 + 频率）
     */
    public static final String CHAT_STREAM_LIMIT = "chat:stream:limit:";

    /**
     * 拼接流式接口限流 key
     *
     * @param userId 用户ID
     * @return 完整 key
     */
    public static String chatStreamLimit(Long userId) {
        return CHAT_STREAM_LIMIT + userId;
    }
}
