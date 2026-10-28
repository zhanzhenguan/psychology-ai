package com.zzg.psychologyai.infrastructure.persistence.read;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表查询投影（非表映射 PO）：
 * 由 ConsultationSessionMapper.xml 的 selectSessionSummaryPage 返回，
 * 聚合 consultation_session + user（昵称）+ consultation_message（条数/最后消息）。
 */
@Data
public class ConsultationSessionDO implements Serializable {

    /** 会话ID */
    private Long id;

    /** 归属用户ID */
    private Long userId;

    /** 用户昵称（来源 user.nickname） */
    private String userNickname;

    /** 会话标题 */
    private String sessionTitle;

    /** 会话开始时间 */
    private LocalDateTime startedAt;

    /** 消息总条数（未删除） */
    private Integer messageCount;

    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;

    /** 最后一条消息内容 */
    private String lastMessageContent;

    /** 会话时长（分钟）：startedAt 与最后一条消息时间之差 */
    private Integer durationMinutes;
}
