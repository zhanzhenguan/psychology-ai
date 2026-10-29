package com.zzg.psychologyai.application;

import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.domain.enums.AnalysisTaskStatusEnum;
import com.zzg.psychologyai.domain.model.AiAnalysisTask;
import com.zzg.psychologyai.domain.repository.AiAnalysisTaskRepository;
import com.zzg.psychologyai.domain.repository.EmotionDiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * AI 分析任务应用服务（写侧）：驱动任务状态机流转，并在终态时联动更新情绪日记。
 * <p>
 * 调度器按 claim →（调用大模型，无事务）→ complete / fail 的顺序编排，
 * 大模型调用不包在事务内，避免长事务占用数据库连接。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisTaskAppService {

    private final AiAnalysisTaskRepository aiAnalysisTaskRepository;
    private final EmotionDiaryRepository emotionDiaryRepository;

    /**
     * 认领待处理任务：置为处理中。任务不存在或已被认领时返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean claim(Long taskId) {
        Optional<AiAnalysisTask> optional = aiAnalysisTaskRepository.findById(taskId);
        if (optional.isEmpty() || optional.get().getStatus() != AnalysisTaskStatusEnum.PENDING) {
            return false;
        }
        AiAnalysisTask task = optional.get();
        task.markProcessing();
        aiAnalysisTaskRepository.update(task);
        return true;
    }

    /**
     * 分析成功：任务置完成终态并回填结果，同事务更新日记的 AI 分析字段
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long taskId, String analysisJson) {
        AiAnalysisTask task = aiAnalysisTaskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "分析任务不存在"));
        task.markCompleted(analysisJson);
        aiAnalysisTaskRepository.update(task);

        emotionDiaryRepository.findById(task.getDiaryId()).ifPresent(diary -> {
            diary.applyAiAnalysis(analysisJson);
            emotionDiaryRepository.save(diary);
        });
    }

    /**
     * 分析失败：任务累加重试，未达上限回到待处理；重试耗尽进入失败终态时联动日记置 FAILED
     */
    @Transactional(rollbackFor = Exception.class)
    public void fail(Long taskId, String errorMessage) {
        AiAnalysisTask task = aiAnalysisTaskRepository.findById(taskId)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "分析任务不存在"));
        task.markFailed(errorMessage);
        aiAnalysisTaskRepository.update(task);

        if (task.isFinalFailed()) {
            emotionDiaryRepository.findById(task.getDiaryId()).ifPresent(diary -> {
                diary.markAnalysisFailed();
                emotionDiaryRepository.save(diary);
            });
            log.warn("AI 分析任务重试耗尽进入失败终态, taskId={}, diaryId={}", taskId, task.getDiaryId());
        }
    }
}
