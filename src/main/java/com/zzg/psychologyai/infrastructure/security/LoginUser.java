package com.zzg.psychologyai.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 登录用户主体：登录认证与 JWT 认证统一使用该对象作为 SecurityContext 的 principal，
 * 便于后续业务从上下文直接获取当前用户ID
 */
@Getter
public class LoginUser implements UserDetails, Serializable {

    /**
     * 用户类型：管理员
     */
    private static final int USER_TYPE_ADMIN = 2;

    /**
     * 用户状态：正常
     */
    private static final int USER_STATUS_ENABLED = 1;

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码密文（JWT 认证场景下为 null，不参与校验）
     */
    private final String password;

    /**
     * 用户类型：1普通用户 2管理员
     */
    private final Integer userType;

    /**
     * 账号是否可用
     */
    private final boolean enabled;

    /**
     * 权限集合
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 完整构造：登录认证时使用
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param password 密码密文
     * @param userType 用户类型
     * @param status   账号状态
     */
    public LoginUser(Long userId, String username, String password, Integer userType, Integer status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.enabled = Integer.valueOf(USER_STATUS_ENABLED).equals(status);
        this.authorities = resolveAuthorities(userType);
    }

    /**
     * 精简构造：JWT 认证时使用，密码与状态按已签发令牌默认处理
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param userType 用户类型
     */
    public LoginUser(Long userId, String username, Integer userType) {
        this(userId, username, null, userType, USER_STATUS_ENABLED);
    }

    /**
     * 按用户类型映射权限：userType 2 为管理员，其余为普通用户（见 docs/ai-agent.sql user 表）
     */
    private static List<GrantedAuthority> resolveAuthorities(Integer userType) {
        if (Integer.valueOf(USER_TYPE_ADMIN).equals(userType)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
