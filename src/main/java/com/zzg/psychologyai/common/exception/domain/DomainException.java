package com.zzg.psychologyai.common.exception.domain;

import com.zzg.psychologyai.common.exception.BusinessException;

/**
 * 领域异常（应用层）：表示领域层的业务异常。
 * <p>
 *  用于处理领域层的业务异常，如实体验证失败、领域规则违规等。
 * <p>
 * 该异常应被应用层捕获并处理，避免直接暴露给客户端。
 * @author: zzg
 * @date: 2026年10月16日  19:13
 * @version: 1.0
 */
public class DomainException extends BusinessException {
    public DomainException(int code, String message, String aggregateId) {
        super(code, message, aggregateId);

    }


}
