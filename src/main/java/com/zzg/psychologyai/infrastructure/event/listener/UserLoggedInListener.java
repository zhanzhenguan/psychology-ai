package com.zzg.psychologyai.infrastructure.event.listener;

import com.zzg.psychologyai.domain.event.UserLoggedInEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户登录事件监听器（基础设施层）：
 * 事务提交后触发跨聚合协作 —— 回风控系统验证登录风险。
 * 风控系统尚未接入，当前以日志占位，后续替换为真实风控适配器。
 * <p>
 * 事件为 record，访问器直接使用 userId() / username()。
 */
@Slf4j
@Component
public class UserLoggedInListener {

    /**
     * 处理登录成功事件（事务提交后执行）
     *
     * @param event 登录成功事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserLoggedIn(UserLoggedInEvent event) {
        log.info("【领域事件】userId={}, type=UserLoggedIn, occurredOn={}",
                event.userId(), event.occurredOn());
        verifyRiskControl(event);
    }

    /**
     * 回风控系统验证登录风险
     *
     * @param event 登录成功事件
     */
    private void verifyRiskControl(UserLoggedInEvent event) {
        // TODO 接入真实风控系统后替换：以 userId / 用户名上报并校验登录风险
        log.info("【事件驱动】登录成功，回风控系统验证, userId={}, username={}",
                event.userId(), event.username());
    }
}
