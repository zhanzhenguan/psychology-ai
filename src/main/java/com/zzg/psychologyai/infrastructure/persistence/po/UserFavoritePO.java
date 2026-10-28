package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户文章收藏持久化对象：对应 docs/ai-agent.sql 的 user_favorite 表。
 * <p>
 * 审计字段继承自 BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_favorite")
public class UserFavoritePO extends BaseEntity {

    /**
     * 主键ID（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID（UUID）
     */
    private String articleId;

    /**
     * 状态：1有效 0已取消
     */
    private Integer status;
}
