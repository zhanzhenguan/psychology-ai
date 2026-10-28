package com.zzg.psychologyai.infrastructure.persistence.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zzg.psychologyai.infrastructure.security.CurrentUserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 公共字段自动填充处理器（基础设施层）：
 * createdBy / updatedBy 从 SecurityContext 取当前登录用户ID，未登录（如注册接口白名单）回填 0 表示系统。
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 未登录时的操作人ID（系统/匿名）
     */
    private static final Long SYSTEM_OPERATOR_ID = 0L;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 从 SecurityContext 取当前登录用户ID
        Long operator = CurrentUserUtils.getCurrentUserId();
        if (operator == null) {
            operator = SYSTEM_OPERATOR_ID;
        }
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createdBy", Long.class, operator);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, operator);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long operator = CurrentUserUtils.getCurrentUserId();
        if (operator == null) {
            operator = SYSTEM_OPERATOR_ID;
        }
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, operator);
    }
}
