package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.zzg.psychologyai.domain.model.ConsultationMessage;
import com.zzg.psychologyai.domain.model.ConsultationSession;
import com.zzg.psychologyai.domain.repository.ConsultationSessionRepository;
import com.zzg.psychologyai.infrastructure.persistence.converter.ConsultationMessageConverter;
import com.zzg.psychologyai.infrastructure.persistence.converter.ConsultationSessionConverter;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationMessageMapper;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationSessionMapper;
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
    private final ConsultationSessionConverter sessionConverter;
    private final ConsultationMessageConverter messageConverter;

    @Override
    public ConsultationSession save(ConsultationSession session) {
        ConsultationSessionPO po = sessionConverter.toPO(session);
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
        return po == null ? Optional.empty() : Optional.of(sessionConverter.toDomain(po));
    }

    @Override
    public void deleteById(Long id) {
        sessionMapper.deleteById(id);
    }

    @Override
    public List<ConsultationMessage> findMessagesBySessionId(Long sessionId) {
        return messageMapper.selectBySessionId(sessionId).stream().map(messageConverter::toDomain).toList();
    }
}
