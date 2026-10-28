package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户账号状态枚举
 */
public enum UserStatusEnum {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 正常 */
    ENABLED(1, "正常");

    @EnumValue
    private final int value;

    private final String label;

    UserStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static UserStatusEnum of(Integer value) {
        if (value == null) {
            return ENABLED;
        }
        for (UserStatusEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return ENABLED;
    }
}
