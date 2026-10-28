package com.zzg.psychologyai.application.view;

import com.zzg.psychologyai.domain.model.User;

import java.io.Serializable;

/**
 * 用户读模型（Read Model）：CQRS 读侧投影，扁平、无值对象、无行为。
 * 返回前端的 gender/userType 为数据库 Integer 值，展示名由接口层按枚举 label 渲染。
 */
public record UserView(Long id,
                       String username,
                       String email,
                       String phone,
                       String nickname,
                       String avatar,
                       Integer gender,
                       Integer userType) implements Serializable {

    /**
     * 写操作成功后从聚合根构造读模型
     */
    public static UserView from(User user) {

        return new UserView(
                user.getId(),
                user.getUsername() == null ? null : user.getUsername().value(),
                user.getEmail() == null ? null : user.getEmail().value(),
                user.getPhone() == null ? null : user.getPhone().value(),
                user.getNickname(),
                user.getAvatar(),
                user.getGender() == null ? null : user.getGender().getValue(),
                user.getUserType() == null ? null : user.getUserType().getValue()
        );
    }


}
