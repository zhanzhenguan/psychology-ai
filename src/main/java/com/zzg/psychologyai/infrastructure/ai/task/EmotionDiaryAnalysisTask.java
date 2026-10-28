package com.zzg.psychologyai.infrastructure.ai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzg.psychologyai.infrastructure.ai.result.EmotionAnalysisResult;
import com.zzg.psychologyai.infrastructure.ai.service.EmotionDiaryAiAnalyzer;
import com.zzg.psychologyai.infrastructure.persistence.mapper.EmotionDiaryMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.EmotionDiaryPO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 情绪日记 AI 分析定时任务：轮询 PENDING / FAIL 状态的日记，调大模型分析后更新结果。
 * 分析失败标记 FAIL，下次轮询自动重试。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmotionDiaryAnalysisTask {

    private static final int BATCH_SIZE = 10;

    private final EmotionDiaryMapper emotionDiaryMapper;
    private final EmotionDiaryAiAnalyzer analyzer;
    private final ObjectMapper objectMapper;

    /**
     * 每 5 分钟扫一次待分析日记（PENDING + FAIL 重试）
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void analyzePendingDiaries() {
        LambdaQueryWrapper<EmotionDiaryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EmotionDiaryPO::getAiAnalysisStatus, List.of("PENDING", "FAIL"))
                .orderByAsc(EmotionDiaryPO::getCreateTime)
                .last("LIMIT " + BATCH_SIZE);
        List<EmotionDiaryPO> pendingList = emotionDiaryMapper.selectList(wrapper);
        if (pendingList.isEmpty()) {
            return;
        }
        log.info("开始 AI 分析情绪日记, 数量={}", pendingList.size());

        for (EmotionDiaryPO po : pendingList) {
            try {
                EmotionAnalysisResult result = analyzer.analyze(
                        po.getMoodScore(),
                        po.getDominantEmotion(),
                        po.getEmotionTriggers(),
                        po.getDiaryContent(),
                        po.getSleepQuality(),
                        po.getStressLevel(),
                        System.currentTimeMillis()
                );
                if (result == null) {
                    markFailed(po.getId());
                    continue;
                }
                String json = objectMapper.writeValueAsString(result);
                EmotionDiaryPO update = new EmotionDiaryPO();
                update.setId(po.getId());
                update.setAiAnalysisStatus("COMPLETED");
                update.setAiEmotionAnalysis(json);
                update.setAiAnalysisUpdatedAt(LocalDateTime.now());
                emotionDiaryMapper.updateById(update);
                log.info("情绪日记分析完成, diaryId={}", po.getId());
            } catch (Exception e) {
                log.error("情绪日记分析异常, diaryId={}", po.getId(), e);
                markFailed(po.getId());
            }
        }
    }

    private void markFailed(Long diaryId) {
        EmotionDiaryPO update = new EmotionDiaryPO();
        update.setId(diaryId);
        update.setAiAnalysisStatus("FAIL");
        update.setAiAnalysisUpdatedAt(LocalDateTime.now());
        emotionDiaryMapper.updateById(update);
        log.warn("情绪日记分析失败, diaryId={}", diaryId);
    }
}
