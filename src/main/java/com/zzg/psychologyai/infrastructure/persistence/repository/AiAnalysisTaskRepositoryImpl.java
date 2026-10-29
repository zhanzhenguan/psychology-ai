package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.zzg.psychologyai.domain.model.AiAnalysisTask;
import com.zzg.psychologyai.domain.repository.AiAnalysisTaskRepository;
import com.zzg.psychologyai.infrastructure.persistence.converter.AiAnalysisTaskConverter;
import com.zzg.psychologyai.infrastructure.persistence.mapper.AiAnalysisTaskMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.AiAnalysisTaskPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI 分析任务仓储实现。
 * <p>
 * errorMessage / result 在 PO 上声明 updateStrategy=IGNORED，
 * updateById 时可空字段也能被显式清空；update_time 由 MetaObjectHandler 自动填充。
 */
@Repository
@RequiredArgsConstructor
public class AiAnalysisTaskRepositoryImpl implements AiAnalysisTaskRepository {

    private final AiAnalysisTaskMapper aiAnalysisTaskMapper;
    private final AiAnalysisTaskConverter aiAnalysisTaskConverter;

    @Override
    public Long insert(AiAnalysisTask task) {
        AiAnalysisTaskPO po = aiAnalysisTaskConverter.toPO(task);
        aiAnalysisTaskMapper.insert(po);
        return po.getId();
    }

    @Override
    public void update(AiAnalysisTask task) {
        AiAnalysisTaskPO po = aiAnalysisTaskConverter.toPO(task);
        aiAnalysisTaskMapper.updateById(po);
    }

    @Override
    public Optional<AiAnalysisTask> findById(Long id) {
        AiAnalysisTaskPO po = aiAnalysisTaskMapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(aiAnalysisTaskConverter.toDomain(po));
    }
}
