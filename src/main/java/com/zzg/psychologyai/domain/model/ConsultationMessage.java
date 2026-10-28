package com.zzg.psychologyai.domain.model;

import com.zzg.psychologyai.domain.enums.MessageTypeEnum;
import com.zzg.psychologyai.domain.enums.SenderTypeEnum;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 咨询消息实体（对应 consultation_message 表）
 */
public class ConsultationMessage {

    private Long id;
    private Long sessionId;

    /** 发送者类型：1用户 2AI助手 */
    private SenderTypeEnum senderType;

    /** 消息类型：1文本 2图片 3语音 */
    private MessageTypeEnum messageType;

    private String content;

    /** 情绪标签（如：开心、焦虑、平静） */
    private String emotionTag;

    /** AI模型名称 */
    private String aiModel;

    private LocalDateTime createTime;







    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEmotionTag() { return emotionTag; }
    public void setEmotionTag(String emotionTag) { this.emotionTag = emotionTag; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public SenderTypeEnum getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderTypeEnum senderType) {
        this.senderType = senderType;
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultationMessage that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
