package com.zzg.psychologyai.application.view;

import com.zzg.psychologyai.infrastructure.persistence.read.ConsultationSessionDO;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话读模型（对齐目标后端响应字段）
 */
public record ChatSessionView(
        Long id,
        Long userId,
        String userNickname,
        String sessionTitle,
        LocalDateTime startedAt,
        Integer durationMinutes,
        Integer messageCount,
        String lastMessageContent,
        LocalDateTime lastMessageTime
) implements Serializable {

    /**
     * 查询投影 → 读模型（读侧 CQRS，与 ArticleQueryServiceImpl 直用 PO 的先例一致）
     */
    public static ChatSessionView from(ConsultationSessionDO summary) {
        return new ChatSessionView(
                summary.getId(),
                summary.getUserId(),
                summary.getUserNickname(),
                summary.getSessionTitle(),
                summary.getStartedAt(),
                summary.getDurationMinutes(),
                summary.getMessageCount(),
                summary.getLastMessageContent(),
                summary.getLastMessageTime()
        );
    }
}
