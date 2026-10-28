package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 文章状态枚举
 */
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架");

    @EnumValue
    private final int value;
    private final String label;

    ArticleStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static ArticleStatusEnum of(Integer value) {
        if (value == null) {
            return DRAFT;
        }
        for (ArticleStatusEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return DRAFT;
    }
}
