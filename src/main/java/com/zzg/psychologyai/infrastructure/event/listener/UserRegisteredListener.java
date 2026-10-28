package com.zzg.psychologyai.infrastructure.event.listener;

import com.zzg.psychologyai.domain.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户注册事件监听器（基础设施层）：
 * 事务提交后触发跨聚合协作 —— 发送欢迎邮件。
 * 外部系统尚未接入，当前以日志占位，后续替换为真实邮件服务适配器。
 * <p>
 * 积分发放属于未来积分聚合的职责，等积分上下文落地后再新增对应监听器，当前不在此占位。
 */
@Slf4j
@Component
public class UserRegisteredListener {

    /**
     * 处理注册成功事件（事务提交后执行，避免外部调用受主事务回滚影响）
     *
     * @param event 注册成功事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("【领域事件】userId={}, type=UserRegistered, occurredOn={}",
                event.userId(), event.occurredOn());
        sendWelcomeMail(event);
    }

    /**
     * 发送欢迎邮件
     *
     * @param event 注册成功事件
     */
    private void sendWelcomeMail(UserRegisteredEvent event) {
        // TODO 接入真实邮件服务后替换：向 event.email() 发送欢迎邮件
        log.info("【事件驱动】注册成功，发送欢迎邮件, userId={}, email={}", event.userId(), event.email());
    }
}
