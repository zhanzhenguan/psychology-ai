package com.zzg.psychologyai.domain.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件元数据工厂：
 * 统一为 record 形态的领域事件填充 eventId / occurredOn，避免每个事件类手写重复样板。
 * <p>
 * 领域事件在聚合根行为方法内构造时调用：
 * <pre>
 * registerEvent(UserRegisteredEvent.of(userId, username));
 * </pre>
 * of 方法内部再委托到这里生成元数据。
 */
public final class DomainEvents {

    private DomainEvents() {
    }

    /**
     * 生成新的事件唯一 ID
     *
     * @return UUID 字符串
     */
    public static String newEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成事件发生时间（UTC）
     *
     * @return 当前时间戳
     */
    public static Instant now() {
        return Instant.now();
    }
}
