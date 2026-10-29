package com.zzg.psychologyai.domain.repository;

import com.zzg.psychologyai.domain.model.AiAnalysisTask;

import java.util.Optional;

/**
 * AI 分析任务仓储端口
 */
public interface AiAnalysisTaskRepository {

    /**
     * 新增任务（插入并回写生成的主键）
     *
     * @return 任务ID
     */
    Long insert(AiAnalysisTask task);

    /**
     * 更新任务（按 id 全量更新业务字段，可空字段显式写 null）
     */
    void update(AiAnalysisTask task);

    Optional<AiAnalysisTask> findById(Long id);
}
