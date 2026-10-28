package com.zzg.psychologyai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 认证通过后的用户信息（应用层对象）：由凭证校验端口返回，供令牌签发使用
 */
@Data
@AllArgsConstructor
public class AuthenticatedUser implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型：1普通用户 2管理员
     */
    private Integer userType;
}
