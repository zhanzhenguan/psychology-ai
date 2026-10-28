package com.zzg.psychologyai.domain.valueobject;

import com.zzg.psychologyai.common.exception.BizException;

import java.io.Serializable;

/**
 * 用户名值对象（Record 形态，不可变）：构造即校验，保证领域内不存在非法用户名。
 *
 * @param value 用户名明文
 */
public record Username(String value) implements Serializable {

    /**
     * 最小长度
     */
    private static final int MIN_LENGTH = 4;

    /**
     * 最大长度
     */
    private static final int MAX_LENGTH = 64;

    /**
     * 仅允许字母、数字与下划线
     */
    private static final String PATTERN = "^[a-zA-Z0-9_]+$";

    /**
     * Record 紧凑构造器：校验 + trim
     *
     * @param value 用户名原文
     */
    public Username {
        if (value == null || value.isBlank()) {
            throw new BizException("用户名不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new BizException("用户名长度需为 4-64 位");
        }
        if (!trimmed.matches(PATTERN)) {
            throw new BizException("用户名仅支持字母、数字与下划线");
        }
        value = trimmed;
    }

    @Override
    public String toString() {
        return value;
    }
}
