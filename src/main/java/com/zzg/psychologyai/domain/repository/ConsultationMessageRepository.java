package com.zzg.psychologyai.domain.repository;

import com.zzg.psychologyai.domain.model.ConsultationMessage;

import java.util.Optional;

/**
 * @author: zzg
 * @date: 2026年10月16日  23:39
 * @version: 1.0
 */
public interface ConsultationMessageRepository {

    ConsultationMessage save(ConsultationMessage message);

    /**
     * 保存用户发送的消息（sender_type=1）
     */
    ConsultationMessage saveUserMessage(Long sessionIdLong, String userMessage, String emotionTag);

    /**
     * 保存 AI 助手回复的消息（sender_type=2）
     *
     * @param sessionId 会话ID
     * @param content   AI 完整回复内容
     * @param aiModel   AI 模型名称（写入 ai_model 列）
     */
    ConsultationMessage saveAssistantMessage(Long sessionId, String content, String aiModel);

    /**
     * 查询会话内最新一条消息（按创建时间倒序），用于判断初始消息是否已落库
     *
     * @param sessionId 会话ID
     * @return 最新消息，无消息时为空
     */
    Optional<ConsultationMessage> findLatestBySessionId(Long sessionId);


    void deleteBySessionId(Long sessionId);
}
