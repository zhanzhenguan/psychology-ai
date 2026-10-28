package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 文件归属字段枚举
 */
public enum BusinessFieldEnum {

    COVER("cover", "封面");

    @EnumValue
    private final String value;
    private final String label;

    BusinessFieldEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static BusinessFieldEnum of(String value) {
        if (value == null) {
            return COVER;
        }
        for (BusinessFieldEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return COVER;
    }
}
