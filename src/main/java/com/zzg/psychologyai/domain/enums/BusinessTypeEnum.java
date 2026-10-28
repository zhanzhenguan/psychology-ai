package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 文件上传业务类型枚举
 */
public enum BusinessTypeEnum {

    ARTICLE("ARTICLE", "文章");

    @EnumValue
    private final String value;
    private final String label;

    BusinessTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static BusinessTypeEnum of(String value) {
        if (value == null) {
            return ARTICLE;
        }
        for (BusinessTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return ARTICLE;
    }
}
