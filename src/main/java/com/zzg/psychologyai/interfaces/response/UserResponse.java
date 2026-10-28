package com.zzg.psychologyai.interfaces.response;

import com.zzg.psychologyai.application.view.UserView;
import com.zzg.psychologyai.interfaces.assembler.UserAssembler;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息响应体：仅暴露必要字段，不返回密码等敏感信息
 */
@Data
@Builder
public class UserResponse implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 性别：0未知 1男 2女
     */
    private Integer gender;
    private String genderDisplayName;

    /**
     * 用户类型：1普通用户 2管理员
     */
    private Integer userType;
    private String userTypeDisplayName;





}
