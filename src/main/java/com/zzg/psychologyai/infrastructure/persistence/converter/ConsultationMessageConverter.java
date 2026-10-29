package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.domain.enums.MessageTypeEnum;
import com.zzg.psychologyai.domain.enums.SenderTypeEnum;
import com.zzg.psychologyai.domain.model.ConsultationMessage;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationMessagePO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 咨询消息持久化转换器（MapStruct 编译期生成实现）：ConsultationMessage ↔ ConsultationMessagePO，
 * SenderTypeEnum / MessageTypeEnum 与码值列的转换由 default 方法承载
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsultationMessageConverter {

    ConsultationMessagePO toPO(ConsultationMessage message);

    ConsultationMessage toDomain(ConsultationMessagePO po);

    default Integer senderTypeToCode(SenderTypeEnum senderType) {
        return senderType == null ? null : senderType.getValue();
    }

    default SenderTypeEnum codeToSenderType(Integer code) {
        return SenderTypeEnum.of(code);
    }

    default Integer messageTypeToCode(MessageTypeEnum messageType) {
        return messageType == null ? null : messageType.getValue();
    }

    default MessageTypeEnum codeToMessageType(Integer code) {
        return MessageTypeEnum.of(code);
    }
}
