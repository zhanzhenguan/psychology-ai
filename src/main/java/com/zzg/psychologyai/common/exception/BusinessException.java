package com.zzg.psychologyai.common.exception;

/**
 * @author: zzg
 * @date: 2026年10月16日  19:10
 * @version: 1.0
 */
public abstract class BusinessException extends RuntimeException {

    private final int code;
    private final String aggregateId;

    protected BusinessException(int code, String message) {
        this(code, message, null);
    }

    protected BusinessException(int code, String message, String aggregateId) {
        super(message);
        this.code = code;
        this.aggregateId = aggregateId;
    }

    public int getCode() { return code; }
    public String getAggregateId() { return aggregateId; }

    /** 是否应该触发事务回滚（默认 true，子类可覆盖） */
    public boolean shouldRollback() { return true; }
}
