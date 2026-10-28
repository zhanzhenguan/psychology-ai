package com.zzg.psychologyai.domain.valueobject;

import com.zzg.psychologyai.common.exception.BizException;

import java.io.Serializable;

/**
 * 邮箱值对象（Record 形态，不可变）：构造即校验；业务上邮箱为可选字段，空值由 ofNullable 表达为 null。
 *
 * @param value 邮箱明文
 */
public record Email(String value) implements Serializable {

    /**
     * 最大长度
     */
    private static final int MAX_LENGTH = 128;

    /**
     * 邮箱格式（轻量校验，与接口层保持一致）
     */
    private static final String PATTERN = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";

    /**
     * Record 紧凑构造器：校验 + trim，非法直接抛业务异常
     *
     * @param value 邮箱原文
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new BizException("邮箱不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new BizException("邮箱长度不能超过 128");
        }
        if (!trimmed.matches(PATTERN)) {
            throw new BizException("邮箱格式不正确");
        }
        value = trimmed;
    }

    /**
     * 可选邮箱：空白串或 null 返回 null，否则走紧凑构造器校验
     *
     * @param value 邮箱原文
     * @return 邮箱值对象或 null
     */
    public static Email ofNullable(String value) {
        return (value == null || value.isBlank()) ? null : new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
