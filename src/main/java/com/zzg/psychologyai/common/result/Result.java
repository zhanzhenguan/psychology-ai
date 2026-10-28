package com.zzg.psychologyai.common.result;

import com.zzg.psychologyai.common.constant.TraceIdConstants;
import com.zzg.psychologyai.common.enums.ResultCode;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一接口响应体，所有 Controller 返回值必须经此类包装
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 状态码，取值见 {@link ResultCode}
     */
    private int code;

    /**
     * 提示消息，面向用户的安全文案
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 链路追踪 ID，与日志 MDC 中的 traceId 一致，便于问题排查
     */
    private String traceId;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = MDC.get(TraceIdConstants.TRACE_ID);
    }

    /**
     * 成功（无数据）
     *
     * @param <T> 业务数据类型
     * @return 统一响应体
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功（携带数据）
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 统一响应体
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 失败（使用状态码枚举默认消息）
     *
     * @param resultCode 状态码枚举
     * @param <T>        业务数据类型
     * @return 统一响应体
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败（状态码枚举 + 自定义消息）
     *
     * @param resultCode 状态码枚举
     * @param message    自定义提示消息
     * @param <T>        业务数据类型
     * @return 统一响应体
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 失败（自定义状态码与消息）
     *
     * @param code    状态码
     * @param message 提示消息
     * @param <T>     业务数据类型
     * @return 统一响应体
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }


}
