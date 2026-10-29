package com.zzg.psychologyai.infrastructure.ai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzg.psychologyai.application.AiAnalysisTaskAppService;
import com.zzg.psychologyai.domain.enums.AnalysisTaskStatusEnum;
import com.zzg.psychologyai.domain.model.EmotionDiary;
import com.zzg.psychologyai.domain.port.EmotionAnalysisPort;
import com.zzg.psychologyai.domain.repository.EmotionDiaryRepository;
import com.zzg.psychologyai.infrastructure.persistence.mapper.AiAnalysisTaskMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.AiAnalysisTaskPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * AI 分析任务调度器：以 ai_analysis_task 为驱动表轮询待处理任务，
 * 认领后关联情绪日记提交大模型，按结果由应用服务回写任务表与日记表。
 * <p>
 * 失败重试由任务聚合控制：未达上限自动回到待处理进入下轮，达到上限置失败终态。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalysisTaskScheduler {

    private static final int BATCH_SIZE = 10;

    private final AiAnalysisTaskMapper aiAnalysisTaskMapper;
    private final EmotionDiaryRepository emotionDiaryRepository;
    private final EmotionAnalysisPort emotionAnalysisPort;
    private final AiAnalysisTaskAppService aiAnalysisTaskAppService;

    /**
     * 每 5 分钟扫描待处理任务（按优先级、创建时间升序）
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void processPendingTasks() {
        LambdaQueryWrapper<AiAnalysisTaskPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAnalysisTaskPO::getStatus, AnalysisTaskStatusEnum.PENDING.getCode())
                .orderByAsc(AiAnalysisTaskPO::getPriority)
                .orderByAsc(AiAnalysisTaskPO::getCreateTime)
                .last("LIMIT " + BATCH_SIZE);
        List<AiAnalysisTaskPO> tasks = aiAnalysisTaskMapper.selectList(wrapper);
        if (tasks.isEmpty()) {
            return;
        }
        log.info("开始处理 AI 分析任务, 数量={}", tasks.size());
        for (AiAnalysisTaskPO po : tasks) {
            processOne(po);
        }
    }

    private void processOne(AiAnalysisTaskPO po) {
        Long taskId = po.getId();
        try {
            // 认领失败说明已被其他执行者处理或状态已变更，直接跳过
            if (!aiAnalysisTaskAppService.claim(taskId)) {
                return;
            }
            Optional<EmotionDiary> diaryOptional = emotionDiaryRepository.findById(po.getDiaryId());
            if (diaryOptional.isEmpty()) {
                aiAnalysisTaskAppService.fail(taskId, "关联情绪日记不存在, diaryId=" + po.getDiaryId());
                return;
            }
            Optional<String> analysisJson = emotionAnalysisPort.analyze(diaryOptional.get());
            if (analysisJson.isPresent()) {
                aiAnalysisTaskAppService.complete(taskId, analysisJson.get());
                log.info("AI 分析任务完成, taskId={}, diaryId={}", taskId, po.getDiaryId());
            } else {
                aiAnalysisTaskAppService.fail(taskId, "大模型返回为空或结果解析失败");
            }
        } catch (Exception e) {
            log.error("AI 分析任务处理异常, taskId={}, diaryId={}", taskId, po.getDiaryId(), e);
            failQuietly(taskId, e.getMessage());
        }
    }

    private void failQuietly(Long taskId, String errorMessage) {
        try {
            aiAnalysisTaskAppService.fail(taskId, errorMessage);
        } catch (Exception ex) {
            log.error("AI 分析任务失败回写异常, taskId={}", taskId, ex);
        }
    }
}
