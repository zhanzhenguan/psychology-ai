package com.zzg.psychologyai.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 当前登录用户工具类：从 SecurityContextHolder（ThreadLocal）读取 JWT 过滤器写入的 LoginUser。
 * <p>
 * 调用时机：必须在 Web 请求线程内、JwtAuthenticationFilter 执行之后。
 * 异步线程、定时任务、消息消费等脱离 Web 线程的场景拿不到，需显式传递用户ID。
 */
public final class CurrentUserUtils {

    private CurrentUserUtils() {
    }

    /**
     * 读取当前登录用户 principal
     *
     *
     * @return LoginUser，未登录时返回空
     */
    public static Optional<LoginUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return Optional.of(loginUser);
        }
        return Optional.empty();
    }

    /**
     * 当前登录用户ID，未登录返回 null
     *
     * @return 用户ID或 null
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().map(LoginUser::getUserId).orElse(null);
    }

    /**
     * 当前登录用户名，未登录返回 null
     *
     * @return 用户名或 null
     */
    public static String getCurrentUsername() {
        return getCurrentUser().map(LoginUser::getUsername).orElse(null);
    }

    /**
     * 当前登录用户ID，未登录时抛业务异常（用于必须登录才能操作的写接口）
     *
     * @return 用户ID
     */
    public static Long requireCurrentUserId() {
        return getCurrentUser()
                .map(LoginUser::getUserId)
                .orElseThrow(() -> new IllegalStateException("当前未登录，无法获取操作人"));
    }
}
