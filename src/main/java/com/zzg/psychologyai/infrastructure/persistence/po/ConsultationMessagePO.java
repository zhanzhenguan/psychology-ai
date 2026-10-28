package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 咨询消息 PO（对应 consultation_message 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation_message")
public class ConsultationMessagePO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Integer senderType;

    private Integer messageType;

    private String content;

    private String emotionTag;

    private String aiModel;
}
