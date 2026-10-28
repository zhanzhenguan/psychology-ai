package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户性别枚举
 */
public enum GenderEnum {

    /** 未知 */
    UNKNOWN(0, "未知"),
    /** 男 */
    MALE(1, "男"),
    /** 女 */
    FEMALE(2, "女");

    /**
     * 数据库存储值
     */
    @EnumValue
    private final int value;

    /**
     * 展示名
     */
    private final String label;

    GenderEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 按数据库值反查枚举，未匹配返回未知
     *
     * @param value 数据库值
     * @return 对应枚举
     */
    public static GenderEnum of(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (GenderEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
