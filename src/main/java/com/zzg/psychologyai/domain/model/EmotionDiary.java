package com.zzg.psychologyai.domain.model;

import com.zzg.psychologyai.domain.common.model.AggregateRoot;
import com.zzg.psychologyai.domain.enums.AnalysisStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 情绪日记聚合根（对应 emotion_diary 表）
 */
public class EmotionDiary extends AggregateRoot {

    private Long id;
    private Long userId;

    /** 日记日期 */
    private LocalDate diaryDate;

    /** 情绪评分 1-10 */
    private Integer moodScore;

    /** 主导情绪（如：开心、焦虑、平静） */
    private String dominantEmotion;

    /** 情绪触发因素 */
    private String emotionTriggers;

    /** 日记内容 */
    private String diaryContent;

    /** 睡眠质量 1-5 */
    private Integer sleepQuality;

    /** 压力水平 1-5 */
    private Integer stressLevel;

    /** AI情绪分析结果（JSON 字符串） */
    String aiEmotionAnalysis;

    /** AI分析更新时间 */
    LocalDateTime aiAnalysisUpdatedAt;


    /** 分析状态 */
    private AnalysisStatusEnum analysisStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getDiaryDate() { return diaryDate; }
    public void setDiaryDate(LocalDate diaryDate) { this.diaryDate = diaryDate; }
    public Integer getMoodScore() { return moodScore; }
    public void setMoodScore(Integer moodScore) { this.moodScore = moodScore; }
    public String getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
    public String getEmotionTriggers() { return emotionTriggers; }
    public void setEmotionTriggers(String emotionTriggers) { this.emotionTriggers = emotionTriggers; }
    public String getDiaryContent() { return diaryContent; }
    public void setDiaryContent(String diaryContent) { this.diaryContent = diaryContent; }
    public Integer getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(Integer sleepQuality) { this.sleepQuality = sleepQuality; }
    public Integer getStressLevel() { return stressLevel; }
    public void setStressLevel(Integer stressLevel) { this.stressLevel = stressLevel; }

    public AnalysisStatusEnum getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(AnalysisStatusEnum analysisStatus) { this.analysisStatus = analysisStatus; }

    public String getAiEmotionAnalysis() { return aiEmotionAnalysis; }
    public void setAiEmotionAnalysis(String aiEmotionAnalysis) { this.aiEmotionAnalysis = aiEmotionAnalysis; }
    public LocalDateTime getAiAnalysisUpdatedAt() { return aiAnalysisUpdatedAt; }
    public void setAiAnalysisUpdatedAt(LocalDateTime aiAnalysisUpdatedAt) { this.aiAnalysisUpdatedAt = aiAnalysisUpdatedAt; }

    /**
     * 应用 AI 分析结果：回填结构化分析 JSON，状态置为已分析
     */
    public void applyAiAnalysis(String analysisJson) {
        this.aiEmotionAnalysis = analysisJson;
        this.analysisStatus = AnalysisStatusEnum.COMPLETED;
        this.aiAnalysisUpdatedAt = LocalDateTime.now();
    }

    /**
     * 标记 AI 分析失败（分析任务重试耗尽后的终态联动）
     */
    public void markAnalysisFailed() {
        this.analysisStatus = AnalysisStatusEnum.FAILED;
        this.aiAnalysisUpdatedAt = LocalDateTime.now();
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmotionDiary that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
