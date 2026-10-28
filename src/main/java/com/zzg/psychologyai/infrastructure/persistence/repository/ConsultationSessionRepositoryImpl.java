package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.zzg.psychologyai.domain.enums.MessageTypeEnum;
import com.zzg.psychologyai.domain.enums.SenderTypeEnum;
import com.zzg.psychologyai.domain.model.ConsultationMessage;
import com.zzg.psychologyai.domain.model.ConsultationSession;
import com.zzg.psychologyai.domain.repository.ConsultationSessionRepository;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationMessageMapper;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationSessionMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationMessagePO;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationSessionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ConsultationSessionRepositoryImpl implements ConsultationSessionRepository {

    private final ConsultationSessionMapper sessionMapper;
    private final ConsultationMessageMapper messageMapper;

    @Override
    public ConsultationSession save(ConsultationSession session) {
        ConsultationSessionPO po = toPO(session);
        if (session.getId() == null) {
            sessionMapper.insert(po);
            session.setId(po.getId());
        } else {
            sessionMapper.updateById(po);
        }
        return session;
    }

    @Override
    public Optional<ConsultationSession> findById(Long id) {
        ConsultationSessionPO po = sessionMapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(toDomain(po));
    }

    @Override
    public void deleteById(Long id) {
        sessionMapper.deleteById(id);
    }

    @Override
    public List<ConsultationMessage> findMessagesBySessionId(Long sessionId) {
        return messageMapper.selectBySessionId(sessionId).stream().map(this::toMessageDomain).toList();
    }

    private ConsultationSessionPO toPO(ConsultationSession s) {
        ConsultationSessionPO po = new ConsultationSessionPO();
        po.setId(s.getId());
        po.setUserId(s.getUserId());
        po.setSessionTitle(s.getSessionTitle());
        po.setStartedAt(s.getStartedAt());
        po.setLastEmotionAnalysis(s.getLastEmotionAnalysis());
        po.setLastEmotionUpdatedAt(s.getLastEmotionUpdatedAt());
        return po;
    }

    private ConsultationSession toDomain(ConsultationSessionPO po) {
        ConsultationSession s = new ConsultationSession();
        s.setId(po.getId());
        s.setUserId(po.getUserId());
        s.setSessionTitle(po.getSessionTitle());
        s.setStartedAt(po.getStartedAt());
        s.setLastEmotionAnalysis(po.getLastEmotionAnalysis());
        s.setLastEmotionUpdatedAt(po.getLastEmotionUpdatedAt());
        return s;
    }

    private ConsultationMessage toMessageDomain(ConsultationMessagePO po) {
        ConsultationMessage m = new ConsultationMessage();
        m.setId(po.getId());
        m.setSessionId(po.getSessionId());
        m.setSenderType(SenderTypeEnum.of(po.getSenderType()));
        m.setMessageType(MessageTypeEnum.of(po.getMessageType()));
        m.setContent(po.getContent());
        m.setEmotionTag(po.getEmotionTag());
        m.setAiModel(po.getAiModel());
        m.setCreateTime(po.getCreateTime());
        return m;
    }
}
