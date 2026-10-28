package com.zzg.psychologyai.infrastructure.security;

import cn.hutool.json.JSONUtil;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 安全异常响应处理器：未认证返回 401、无权限返回 403，
 * 统一输出与业务接口一致的 JSON 结构（Result）
 */
@Slf4j
@Component
public class JsonSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        log.warn("未认证访问, uri={}", request.getRequestURI());
        write(response, HttpStatus.UNAUTHORIZED, Result.fail(ResultCode.UNAUTHORIZED));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        log.warn("无权限访问, uri={}", request.getRequestURI());
        write(response, HttpStatus.FORBIDDEN, Result.fail(ResultCode.FORBIDDEN));
    }

    /**
     * 以 UTF-8 JSON 输出统一响应体
     */
    private void write(HttpServletResponse response, HttpStatus status, Result<Void> result) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}
