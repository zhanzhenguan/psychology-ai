package com.zzg.psychologyai.application.dto;

import com.zzg.psychologyai.application.view.UserView;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录结果（写操作附带的读模型返回）：令牌 + 有效期 + 用户读模型，
 * 接口层据此组装登录响应。
 */
@Data
@AllArgsConstructor
public class LoginResult implements Serializable {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 令牌有效期（秒）
     */
    private long expireSeconds;

    /**
     * 登录用户读模型
     */
    private UserView userInfo;
}
