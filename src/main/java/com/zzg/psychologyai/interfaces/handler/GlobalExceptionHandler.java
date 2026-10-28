package com.zzg.psychologyai.interfaces.handler;

import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.common.exception.BusinessException;
import com.zzg.psychologyai.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器（接口层 Web 增强）：集中将异常转换为统一 Result，
 * 不向响应泄露堆栈、SQL、内部类名等敏感信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 请求体参数校验失败，返回字段级中文错误信息
     *
     * @param e 参数校验异常
     * @return 400 + 字段错误明细
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 表单绑定参数校验失败
     *
     * @param e 绑定异常
     * @return 400 + 字段错误明细
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * @Validated 标注在方法参数（@RequestParam/@PathVariable）上的校验失败
     *
     * @param e 约束违反异常
     * @return 400 + 字段错误明细
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(Collectors.joining("；"));
        log.warn("方法参数校验失败: {}", message);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 业务异常：可预期，WARN 级别记录
     *
     * @param e 业务异常
     * @return 业务码 + 安全文案
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }


    /**
     * 业务异常：可预期，WARN 级别记录
     *
     * @param e 业务异常
     * @return 业务码 + 安全文案
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={},aggregateId={}", e.getCode(), e.getMessage(), e.getAggregateId());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 未认证（方法级安全校验抛出）
     *
     * @param e 认证异常
     * @return 401 未登录
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("未认证访问: {}", e.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 已认证但无权限（如 @PreAuthorize 拦截）
     *
     * @param e 访问拒绝异常
     * @return 403 无权限
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("无权限访问: {}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 系统兜底异常：ERROR 级别记录完整堆栈，对前端仅返回固定安全文案与 traceId
     *
     * @param e 未预期异常
     * @return 500 + 固定文案
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.ERROR);
    }
}
