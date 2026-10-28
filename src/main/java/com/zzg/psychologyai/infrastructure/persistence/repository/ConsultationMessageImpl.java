package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzg.psychologyai.domain.enums.MessageTypeEnum;
import com.zzg.psychologyai.domain.enums.SenderTypeEnum;
import com.zzg.psychologyai.domain.model.ConsultationMessage;
import com.zzg.psychologyai.domain.repository.ConsultationMessageRepository;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationMessageMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationMessagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author: zzg
 * @date: 2026年10月16日  23:42
 * @version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class ConsultationMessageImpl implements ConsultationMessageRepository {

    private final ConsultationMessageMapper messageMapper;


    @Override
    public ConsultationMessage save(ConsultationMessage message) {
        ConsultationMessagePO po = toPO(message);
        messageMapper.insert(po);
        message.setId(po.getId());
        return message;
    }

    @Override
    public ConsultationMessage saveUserMessage(Long sessionIdLong, String userMessage, String emotionTag) {
        ConsultationMessagePO po = new ConsultationMessagePO();
        po.setSessionId(sessionIdLong);
        po.setSenderType(1);
        po.setMessageType(1);
        po.setContent(userMessage);
        po.setEmotionTag(emotionTag);
        messageMapper.insert(po);
        return toDomain(po);
    }

    @Override
    public ConsultationMessage saveAssistantMessage(Long sessionId, String content, String aiModel) {
        ConsultationMessagePO po = new ConsultationMessagePO();
        po.setSessionId(sessionId);
        // 发送者类型：2=AI助手
        po.setSenderType(2);
        po.setMessageType(1);
        po.setContent(content);
        po.setAiModel(aiModel);
        messageMapper.insert(po);
        return toDomain(po);
    }

    @Override
    public Optional<ConsultationMessage> findLatestBySessionId(Long sessionId) {
        return Optional.ofNullable(messageMapper.selectLatestBySessionId(sessionId)).map(this::toDomain);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationMessagePO::getSessionId, sessionId);
        messageMapper.delete(wrapper);
    }

    private ConsultationMessagePO toPO(ConsultationMessage message) {
        ConsultationMessagePO po = new ConsultationMessagePO();
        po.setId(message.getId());
        po.setSessionId(message.getSessionId());
        po.setSenderType(message.getSenderType().getValue());
        po.setMessageType(message.getMessageType().getValue());
        po.setContent(message.getContent());
        po.setEmotionTag(message.getEmotionTag());
        po.setAiModel(message.getAiModel());
        po.setCreateTime(message.getCreateTime());
        return po;
    }

    private ConsultationMessage toDomain(ConsultationMessagePO po) {
        ConsultationMessage message = new ConsultationMessage();
        message.setId(po.getId());
        message.setSessionId(po.getSessionId());
        message.setSenderType(SenderTypeEnum.of(po.getSenderType()));
        message.setMessageType(MessageTypeEnum.of(po.getMessageType()));
        message.setContent(po.getContent());
        message.setEmotionTag(po.getEmotionTag());
        message.setAiModel(po.getAiModel());
        message.setCreateTime(po.getCreateTime());
        return message;
    }

}
