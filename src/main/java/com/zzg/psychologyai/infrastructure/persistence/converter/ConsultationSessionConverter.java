package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.domain.model.ConsultationSession;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationSessionPO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 咨询会话持久化转换器（MapStruct 编译期生成实现）：ConsultationSession ↔ ConsultationSessionPO
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsultationSessionConverter {

    ConsultationSessionPO toPO(ConsultationSession session);

    ConsultationSession toDomain(ConsultationSessionPO po);
}
