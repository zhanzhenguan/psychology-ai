package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户账号状态枚举
 */
public enum SenderTypeEnum {

    /** 用户 */
    USER(1, "用户"),
    /** AI助手 */
    ASSISTANT(2, "AI助手");

    @EnumValue
    private final Integer value;

    private final String label;

    SenderTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static SenderTypeEnum of(Integer value) {
        if (value == null) {
            return USER;
        }
        for (SenderTypeEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return USER;
    }
}
