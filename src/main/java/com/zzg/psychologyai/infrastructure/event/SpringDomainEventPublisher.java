package com.zzg.psychologyai.infrastructure.event;

import com.zzg.psychologyai.domain.port.DomainEventPublisher;
import com.zzg.psychologyai.domain.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器的 Spring 实现（基础设施层）：
 * 借助 Spring 事件机制分发领域事件，具体订阅在基础设施层的监听器中完成，
 * 监听器统一以事务提交后（AFTER_COMMIT）的方式执行，保证事件驱动不影响主事务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布领域事件
     *
     * @param event 领域事件
     */
    @Override
    public void publish(DomainEvent event) {
        log.debug("发布领域事件: type={}, eventId={}", event.getClass().getSimpleName(), event.eventId());
        applicationEventPublisher.publishEvent(event);
    }
}
