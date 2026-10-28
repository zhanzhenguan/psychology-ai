package com.zzg.psychologyai.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 登录请求体（接口层）：承载接口契约字段与 Jakarta 校验注解。
 * 由 {@code UserRequestConverter} 转应用层 {@code LoginCommand}。
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 64, message = "用户名长度不能超过 64")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
        String password) implements Serializable {


}
