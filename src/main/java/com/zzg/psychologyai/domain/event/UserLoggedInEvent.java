package com.zzg.psychologyai.domain.event;

import com.zzg.psychologyai.domain.common.event.DomainEvent;
import com.zzg.psychologyai.domain.common.event.DomainEvents;

import java.time.Instant;

/**
 * 用户登录成功领域事件（Record 形态，不可变值对象）：
 * 由用户聚合根在认证通过后登记，事务提交后触发跨聚合协作（回风控系统验证登录风险等）。
 *
 * @param eventId   事件唯一 ID
 * @param occurredOn 事件发生时间
 * @param userId    用户ID
 * @param username  用户名
 */
public record UserLoggedInEvent(String eventId,
                               Instant occurredOn,
                               Long userId,
                               String username) implements DomainEvent {

    /**
     * 构造登录成功事件，自动填充事件元数据
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 登录成功领域事件
     */
    public static UserLoggedInEvent of(Long userId, String username) {
        return new UserLoggedInEvent(DomainEvents.newEventId(), DomainEvents.now(), userId, username);
    }
}
