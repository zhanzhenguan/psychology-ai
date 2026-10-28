package com.zzg.psychologyai.interfaces.filter;

import cn.hutool.core.util.IdUtil;
import com.zzg.psychologyai.common.constant.TraceIdConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 链路追踪过滤器（接口层）：为每个请求生成 traceId 写入 MDC，保证响应体与日志可串联
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put(TraceIdConstants.TRACE_ID, IdUtil.fastSimpleUUID());
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束必须清理，避免线程复用导致 traceId 串号
            MDC.remove(TraceIdConstants.TRACE_ID);
        }
    }
}
