package com.zzg.psychologyai.domain.repository;

import com.zzg.psychologyai.domain.model.ConsultationSession;
import com.zzg.psychologyai.domain.model.ConsultationMessage;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话仓储端口
 */
public interface ConsultationSessionRepository {

    ConsultationSession save(ConsultationSession session);

    Optional<ConsultationSession> findById(Long id);

    void deleteById(Long id);

    List<ConsultationMessage> findMessagesBySessionId(Long sessionId);
}
