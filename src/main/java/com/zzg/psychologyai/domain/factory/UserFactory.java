package com.zzg.psychologyai.domain.factory;

import com.zzg.psychologyai.domain.enums.GenderEnum;
import com.zzg.psychologyai.domain.enums.UserStatusEnum;
import com.zzg.psychologyai.domain.enums.UserTypeEnum;
import com.zzg.psychologyai.domain.model.User;
import com.zzg.psychologyai.domain.valueobject.Email;
import com.zzg.psychologyai.domain.valueobject.Password;
import com.zzg.psychologyai.domain.valueobject.Phone;
import com.zzg.psychologyai.domain.valueobject.Username;

/**
 * 用户领域工厂：封装用户聚合根的创建规则与不变量
 */
public final class UserFactory {

    private UserFactory() {
    }

    /**
     * 创建新用户聚合（注册场景）
     *
     * @param username        用户名
     * @param encodedPassword 加密后的密码密文
     * @param email           邮箱（可选）
     * @param phone           手机号（可选）
     * @param nickname        昵称（可选，缺省取用户名）
     * @param avatar          头像 URL（可选）
     * @param gender          性别（可选，缺省未知）
     * @param userType        用户类型（可选，缺省普通用户）
     * @return 新用户领域对象
     */
    public static User create(String username, String encodedPassword, String email, String phone,
                              String nickname, String avatar, Integer gender, Integer userType) {
        User user = new User();
        user.setUsername(new Username(username));
        user.setPassword(new Password(encodedPassword));
        user.setEmail(Email.ofNullable(email));
        user.setPhone(Phone.ofNullable(phone));
        user.setNickname((nickname == null || nickname.isBlank()) ? username : nickname.trim());
        user.setAvatar(avatar);
        user.setGender(GenderEnum.of(gender));
        user.setUserType(UserTypeEnum.of(userType));
        user.setStatus(UserStatusEnum.ENABLED);
        return user;
    }
}
