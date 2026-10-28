package com.zzg.psychologyai.domain.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * 领域事件标记接口（JDK 17 Record 形态）：
 * 领域内发生的重要业务事实（如注册成功、登录成功、信息变更），
 * 用于跨聚合 / 跨模块的事件驱动协作。
 * <p>
 * 每个具体事件都以 {@code record} 实现本接口，天然不可变、值语义正确；
 * 事件元数据（eventId / occurredOn）由 {@link DomainEvents} 工厂在事件构造时统一填充，
 * 业务字段紧跟其后，形如：
 * <pre>
 * public record UserRegisteredEvent(String eventId, Instant occurredOn, Long userId, String username)
 *         implements DomainEvent {
 *     public static UserRegisteredEvent of(Long userId, String username) {
 *         return new UserRegisteredEvent(DomainEvents.newEventId(), DomainEvents.now(), userId, username);
 *     }
 * }
 * </pre>
 */
public interface DomainEvent extends Serializable {

    /**
     * 事件唯一 ID（UUID 字符串），用于链路追踪与去重
     *
     * @return 事件唯一 ID
     */
    String eventId();

    /**
     * 事件发生时间（UTC 时间戳）
     *
     * @return 事件发生时间
     */
    Instant occurredOn();
}
