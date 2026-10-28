package com.zzg.psychologyai.domain.valueobject;

import com.zzg.psychologyai.common.exception.BizException;

import java.io.Serializable;

/**
 * 手机号值对象（Record 形态，不可变）：构造即校验；业务上手机号为可选字段，空值由 ofNullable 表达为 null。
 *
 * @param value 手机号明文
 */
public record Phone(String value) implements Serializable {

    /**
     * 中国大陆手机号格式
     */
    private static final String PATTERN = "^1[3-9]\\d{9}$";

    /**
     * Record 紧凑构造器：校验 + trim
     *
     * @param value 手机号原文
     */
    public Phone {
        if (value == null || value.isBlank()) {
            throw new BizException("手机号不能为空");
        }
        String trimmed = value.trim();
        if (!trimmed.matches(PATTERN)) {
            throw new BizException("手机号格式不正确");
        }
        value = trimmed;
    }

    /**
     * 可选手机号：空白串或 null 返回 null，否则走紧凑构造器校验
     *
     * @param value 手机号原文
     * @return 手机号值对象或 null
     */
    public static Phone ofNullable(String value) {
        return (value == null || value.isBlank()) ? null : new Phone(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
