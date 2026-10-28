package com.zzg.psychologyai.common.exception;

import com.zzg.psychologyai.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常：可预期的业务校验失败（如手机号已存在），
 * 抛出后由接口层全局异常处理器统一转换为 Result，事务场景下抛出可触发回滚
 */
@Getter
public class BizException extends RuntimeException {

    /**
     * 业务状态码
     */
    private final int code;

    /**
     * 以默认业务失败码抛出
     *
     * @param message 提示消息
     */
    public BizException(String message) {
        this(ResultCode.FAIL, message);
    }

    /**
     * 以状态码枚举默认消息抛出
     *
     * @param resultCode 状态码枚举
     */
    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 以状态码枚举 + 自定义消息抛出
     *
     * @param resultCode 状态码枚举
     * @param message    提示消息
     */
    public BizException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    /**
     * 以自定义状态码与消息抛出
     *
     * @param code    业务状态码
     * @param message 提示消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
