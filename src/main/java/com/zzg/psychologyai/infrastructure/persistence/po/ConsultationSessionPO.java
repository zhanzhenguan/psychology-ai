package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 咨询会话 PO（对应 consultation_session 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation_session")
public class ConsultationSessionPO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sessionTitle;

    private LocalDateTime startedAt;

    private String lastEmotionAnalysis;

    private LocalDateTime lastEmotionUpdatedAt;
}
