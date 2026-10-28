package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户账号状态枚举
 */
public enum MessageTypeEnum {

    /** 文本 */
    TEXT(1, "文本"),
    /** 图片 */
    IMAGE(2, "图片"),
    /** 音频 */
    AUDIO(3, "音频");

    @EnumValue
    private final Integer value;

    private final String label;

    MessageTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static MessageTypeEnum of(Integer value) {
        if (value == null) {
            return TEXT;
        }
        for (MessageTypeEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return TEXT;
    }
}
