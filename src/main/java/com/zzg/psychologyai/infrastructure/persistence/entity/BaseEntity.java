package com.zzg.psychologyai.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公共实体基类（MyBatis-Plus 持久化实体）：对应 docs/ai-agent.sql 各表统一的审计字段，
 * 属基础设施层持久化细节，领域层不感知
 * <p>
 * 主键 id 由各实体自行声明（如 knowledge_article 为 UUID 字符串），
 * 删除标识 deletedFlag 由 mybatis-plus.global-config.db-config 全局配置为逻辑删除字段
 */
@Data
public class BaseEntity implements Serializable {

    /**
     * 创建时间（列 create_time），插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（列 update_time），插入与更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID，插入时自动填充（登录接入前默认 1）
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人ID，插入与更新时自动填充（登录接入前默认 1）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 删除标识：0未删除 1已删除（全局逻辑删除字段，无需 @TableLogic）
     */
    @TableLogic
    private Integer deletedFlag;
}
