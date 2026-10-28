package com.zzg.psychologyai.domain.service;

/**
 * 密码编码端口（领域层定义，基础设施层实现）：
 * 应用层只依赖此接口完成密码单向加密，不感知具体算法与框架
 */
public interface PasswordEncoder {

    /**
     * 加密明文密码
     *
     * @param rawPassword 明文密码
     * @return 密文
     */
    String encode(String rawPassword);

    /**
     * 校验明文与密文是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 密文
     * @return true 表示匹配
     */
    boolean matches(String rawPassword, String encodedPassword);
}
