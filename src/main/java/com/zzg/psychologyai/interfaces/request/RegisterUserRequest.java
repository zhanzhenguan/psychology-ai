package com.zzg.psychologyai.interfaces.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 注册请求体（接口层）：承载接口契约字段与 Jakarta 校验注解。
 * <p>
 * 与应用层 {@code RegisterUserCommand} 字段一一对应，但职责分离：
 * 本类负责接口入参格式校验，应用层 Command 保持纯数据载体。
 * 由 {@code UserRequestConverter} 一行 MapStruct 转 Command。
 */
public record RegisterUserRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 64, message = "用户名长度需为 4-64 位")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名仅支持字母、数字与下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
        String password,

        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过 128")
        String email,

        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Size(max = 64, message = "昵称长度不能超过 64")
        String nickname,

        String avatar,

        Integer gender,

        Integer userType) implements Serializable {
}
