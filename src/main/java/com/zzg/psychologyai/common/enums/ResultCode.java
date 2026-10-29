package com.zzg.psychologyai.common.enums;

import lombok.Getter;

/**
 * 统一响应状态码枚举，业务码语义与 HTTP 状态码保持一致
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 参数校验失败 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录或登录凭证已过期 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 已登录但无权限访问 */
    FORBIDDEN(403, "无权限访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),
    /** 请求频率过高 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    /** 业务处理失败（可预期的业务异常默认码） */
    FAIL(500, "操作失败"),
    /** 系统异常（兜底，对前端隐藏内部细节） */
    ERROR(500, "系统异常，请稍后再试");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 默认提示消息
     */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
