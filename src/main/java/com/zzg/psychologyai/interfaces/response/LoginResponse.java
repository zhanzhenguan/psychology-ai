package com.zzg.psychologyai.interfaces.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应体：JWT 令牌 + 用户信息
 */
@Data
@Builder
public class LoginResponse implements Serializable {

    /**
     * 访问令牌（JWT），后续请求放入 Authorization 请求头
     */
    private String token;

    /**
     * 令牌类型，固定为 Bearer
     */
    private String tokenType;

    /**
     * 令牌有效期（秒）
     */
    private Long expireSeconds;


    /**
     * 角色类型：1普通用户 2管理员
     */
    private Integer roleType;

    /**
     * 登录用户信息
     */
    private UserResponse userInfo;

    /**
     * 组装登录响应
     *
     * @param token         令牌
     * @param expireSeconds 有效期（秒）
     * @param userInfo       用户响应体（由对象转换层转换而来）
     * @return 登录响应体
     */
    public static LoginResponse of(String token, long expireSeconds, UserResponse userInfo) {
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expireSeconds(expireSeconds)
                .roleType(userInfo.getUserType())
                .userInfo(userInfo)
                .build();
        return response;
    }
}
