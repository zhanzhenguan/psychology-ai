package com.zzg.psychologyai.common.constant;

/**
 * 链路追踪常量：traceId 在 MDC 与响应体中的统一键名，
 * 供过滤器与统一响应体共享，避免二者相互依赖
 */
public final class TraceIdConstants {

    /**
     * MDC 与响应体中 traceId 的键名
     */
    public static final String TRACE_ID = "traceId";

    private TraceIdConstants() {
    }
}
