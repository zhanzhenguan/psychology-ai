package com.zzg.psychologyai.domain.valueobject;

import com.zzg.psychologyai.common.exception.BizException;

import java.io.Serializable;

/**
 * 密码值对象（Record 形态，不可变）：承载已单向加密的密文；禁止明文落库，故不在此做明文长度与复杂度校验。
 * toString 固定脱敏，避免日志误打印密文。
 *
 * @param value 密码密文
 */
public record Password(String value) implements Serializable {

    /**
     * Record 紧凑构造器：仅校验非空，密文内容不做格式校验
     *
     * @param value 密码密文
     */
    public Password {
        if (value == null || value.isBlank()) {
            throw new BizException("密码不能为空");
        }
    }

    /**
     * 敏感字段，不输出明文密文
     *
     * @return 固定脱敏串
     */
    @Override
    public String toString() {
        return "Password(***)";
    }
}
