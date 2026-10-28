package com.zzg.psychologyai.domain.event;

import com.zzg.psychologyai.domain.common.event.DomainEvent;
import com.zzg.psychologyai.domain.common.event.DomainEvents;

import java.time.Instant;

/**
 * 用户注册成功领域事件（Record 形态，不可变值对象）：
 * 由用户聚合根在持久化后登记，事务提交后触发跨聚合协作（发送欢迎邮件、发放初始积分等）。
 *
 * @param eventId  事件唯一 ID
 * @param occurredOn 事件发生时间
 * @param userId   用户ID
 * @param username 用户名
 * @param email    邮箱（可能为空）
 */
public record UserRegisteredEvent(String eventId,
                                  Instant occurredOn,
                                  Long userId,
                                  String username,
                                  String email) implements DomainEvent {

    /**
     * 构造注册成功事件，自动填充事件元数据
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param email    邮箱（可能为空）
     * @return 注册成功领域事件
     */
    public static UserRegisteredEvent of(Long userId, String username, String email) {
        return new UserRegisteredEvent(DomainEvents.newEventId(), DomainEvents.now(), userId, username, email);
    }
}
