package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.domain.enums.GenderEnum;
import com.zzg.psychologyai.domain.enums.UserStatusEnum;
import com.zzg.psychologyai.domain.enums.UserTypeEnum;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户持久化对象：对应 docs/ai-agent.sql 的 user 表，
 * 审计字段继承自 BaseEntity；枚举字段由 MyBatis-Plus @EnumValue 自动映射 Integer ↔ 枚举
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserPO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    private String phone;

    private String password;

    private String nickname;

    private String avatar;

    private GenderEnum gender;

    private LocalDate birthday;

    private UserTypeEnum userType;

    private UserStatusEnum status;
}
