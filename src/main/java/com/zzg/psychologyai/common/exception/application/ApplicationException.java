package com.zzg.psychologyai.common.exception.application;

import com.zzg.psychologyai.common.exception.BusinessException;

/**
 * @author: zzg
 * @date: 2026年10月16日  19:14
 * @version: 1.0
 */
public class ApplicationException extends BusinessException {

    public ApplicationException(int code, String message) {
        super(code, message);
    }

    @Override
    public boolean shouldRollback() {
        // 有些应用层错误不需要回滚（比如幂等重复提交）
        return true;
    }
}
