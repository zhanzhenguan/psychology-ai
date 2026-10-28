package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户类型枚举
 */
public enum UserTypeEnum {

    /** 普通用户 */
    NORMAL(1, "普通用户"),
    /** 管理员 */
    ADMIN(2, "管理员");

    @EnumValue
    private final int value;

    private final String label;

    UserTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static UserTypeEnum of(Integer value) {
        if (value == null) {
            return NORMAL;
        }
        for (UserTypeEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return NORMAL;
    }
}
