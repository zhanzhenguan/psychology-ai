package com.zzg.psychologyai.application.view;

import com.zzg.psychologyai.domain.model.ConsultationMessage;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息读模型
 */
public record ChatMessageView(
        Long id,
        Long sessionId,
        Integer senderType,

        String senderTypeDesc,
        Integer messageType,
        String messageTypeDesc,
        String content,
        String aiModel,
        String emotionTag,
        LocalDateTime createdAt,
        Integer contentLength,
        String contentPreview

) implements Serializable {

    public static ChatMessageView from(ConsultationMessage message) {
        return new ChatMessageView(
                message.getId(),
                message.getSessionId(),
                message.getSenderType().getValue(),
                message.getSenderType().getLabel(),
                message.getMessageType().getValue(),
                message.getMessageType().getLabel(),
                message.getContent(),
                message.getAiModel(),
                message.getEmotionTag(),
                message.getCreateTime(),
                message.getContent().length(),
                message.getContent()

        );
    }
}
