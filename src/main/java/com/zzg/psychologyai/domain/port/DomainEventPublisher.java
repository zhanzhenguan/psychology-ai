package com.zzg.psychologyai.domain.port;

import com.zzg.psychologyai.domain.common.event.DomainEvent;
import com.zzg.psychologyai.domain.common.model.AggregateRoot;

import java.util.List;

/**
 * 领域事件发布端口（应用层定义，基础设施层实现）：
 * 应用层只依赖该端口发布事件，不感知具体事件总线与事务钩子。
 * <p>
 * 标准用法：聚合根在行为方法内 registerEvent，应用服务在持久化成功后调用
 * {@link #publishEvents(AggregateRoot)} 一次性取出并发布；事务回滚时事件队列随聚合对象丢弃，
 * 不会产生"幽灵事件"。
 */
public interface DomainEventPublisher {

    /**
     * 发布单个领域事件（用于少数不经聚合根登记的跨模块通知）
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);

    /**
     * 取出并发布聚合根登记的全部领域事件（标准入口）
     *
     * @param aggregateRoot 聚合根
     */
    default void publishEvents(AggregateRoot aggregateRoot) {
        List<DomainEvent> events = aggregateRoot.pullEvents();
        events.forEach(this::publish);
    }
}
