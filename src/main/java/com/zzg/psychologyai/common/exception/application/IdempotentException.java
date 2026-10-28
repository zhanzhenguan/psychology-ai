package com.zzg.psychologyai.common.exception.application;

/**
 * @author: zzg
 * @date: 2026年10月16日  19:16
 * @version: 1.0
 */
public class IdempotentException extends ApplicationException{

    public IdempotentException(String message) {
        super(1111, message);
    }

    @Override
    public boolean shouldRollback() {
        return false;  // 重复提交不回滚
    }

}
