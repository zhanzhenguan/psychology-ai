package com.zzg.psychologyai.domain.common.model;

import com.zzg.psychologyai.domain.common.event.DomainEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类（领域层）：
 * <p>
 * 聚合根在执行业务行为时，通过 {@link #registerEvent(DomainEvent)} 把"已经发生的业务事实"
 * 登记到自身事件队列；应用服务在事务提交前调用 {@link #pullEvents()} 一次性取出并交给
 * {@code DomainEventPublisher} 发布。
 * <p>
 * 这样做的好处：
 * <ul>
 *   <li>事件由聚合根在行为内部自发产生，而不是应用层手工 new，业务语义内聚；</li>
 *   <li>只有落库成功的聚合才会发事件，事务回滚时队列随对象丢弃，不会出现"幽灵事件"；</li>
 *   <li>事件队列是内存态，{@code transient} 修饰，不参与序列化与持久化映射。</li>
 * </ul>
 */
public abstract class AggregateRoot implements Serializable {

    /**
     * 未发布的领域事件队列；transient：不随聚合根序列化、不参与 MapStruct 到 PO 的映射
     */
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 登记一个领域事件（聚合根行为方法内调用）
     *
     * @param event 领域事件
     */
    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }

    /**
     * 取出并清空当前聚合根登记的全部领域事件（应用层 / 基础设施层发布后调用）
     *
     * @return 不可变的事件列表，无事件时返回空列表
     */
    public List<DomainEvent> pullEvents() {
        if (domainEvents.isEmpty()) {
            return Collections.emptyList();
        }
        List<DomainEvent> snapshot = List.copyOf(domainEvents);
        domainEvents.clear();
        return snapshot;
    }
}
