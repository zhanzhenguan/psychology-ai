package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.domain.enums.AnalysisTaskStatusEnum;
import com.zzg.psychologyai.domain.enums.AnalysisTaskTypeEnum;
import com.zzg.psychologyai.domain.model.AiAnalysisTask;
import com.zzg.psychologyai.infrastructure.persistence.po.AiAnalysisTaskPO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * AI 分析任务持久化转换器（MapStruct 编译期生成实现）：AiAnalysisTask（聚合）↔ AiAnalysisTaskPO。
 * 聚合无公开构造，toDomain 为 default 方法走 {@link AiAnalysisTask#restore} 工厂；
 * 状态/类型枚举与码值列的转换由 default 方法承载
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiAnalysisTaskConverter {

    AiAnalysisTaskPO toPO(AiAnalysisTask task);

    default AiAnalysisTask toDomain(AiAnalysisTaskPO po) {
        if (po == null) {
            return null;
        }
        return AiAnalysisTask.restore(
                po.getId(),
                po.getDiaryId(),
                po.getUserId(),
                po.getStatus(),
                po.getTaskType(),
                po.getPriority(),
                po.getRetryCount(),
                po.getMaxRetryCount(),
                po.getErrorMessage(),
                po.getResult(),
                po.getStartedAt(),
                po.getCompletedAt()
        );
    }

    default Integer statusToCode(AnalysisTaskStatusEnum status) {
        return status == null ? null : status.getCode();
    }

    default Integer taskTypeToCode(AnalysisTaskTypeEnum taskType) {
        return taskType == null ? null : taskType.getCode();
    }
}
