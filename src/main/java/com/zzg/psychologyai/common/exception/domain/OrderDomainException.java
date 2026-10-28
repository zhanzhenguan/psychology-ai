package com.zzg.psychologyai.common.exception.domain;

/**
 * @author: zzg
 * @date: 2026年10月16日  19:18
 * @version: 1.0
 */
public class OrderDomainException extends DomainException{

    public OrderDomainException(int code, String message, String aggregateId) {
        super(code, message, aggregateId);
    }

}
