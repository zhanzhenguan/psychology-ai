package com.zzg.psychologyai.infrastructure.persistence.converter;

import com.zzg.psychologyai.domain.enums.AnalysisStatusEnum;
import com.zzg.psychologyai.domain.model.EmotionDiary;
import com.zzg.psychologyai.infrastructure.persistence.po.EmotionDiaryPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 情绪日记持久化转换器（MapStruct 编译期生成实现）：EmotionDiary（聚合）↔ EmotionDiaryPO，
 * AnalysisStatusEnum 与字符串列的码值转换由 default 方法承载
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmotionDiaryConverter {

    @Mapping(source = "analysisStatus", target = "aiAnalysisStatus")
    EmotionDiaryPO toPO(EmotionDiary diary);

    @Mapping(source = "aiAnalysisStatus", target = "analysisStatus")
    EmotionDiary toDomain(EmotionDiaryPO po);

    default String statusToValue(AnalysisStatusEnum status) {
        return status == null ? null : status.getValue();
    }

    default AnalysisStatusEnum valueToStatus(String value) {
        return AnalysisStatusEnum.of(value);
    }
}
