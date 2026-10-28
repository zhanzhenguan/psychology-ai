package com.zzg.psychologyai.application.view;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.zzg.psychologyai.domain.model.EmotionDiary;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 情绪日记读模型
 */
public record EmotionDiaryView(
        Long id,
        Long userId,
        String username,
        String nickname,
        LocalDate diaryDate,
        Integer moodScore,
        String dominantEmotion,
        String emotionTriggers,
        String diaryContent,
        Integer sleepQuality,
        Integer stressLevel,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        Boolean hasAiEmotionAnalysis,
        String aiAnalysisStatus,
        LocalDateTime aiAnalysisUpdatedAt,
        String aiEmotionAnalysis,
        Integer contentLength

) implements Serializable {



    public static EmotionDiaryView from(EmotionDiary diary) {
        return new EmotionDiaryView(
                diary.getId(),
                diary.getUserId(),
                null,
                null,
                diary.getDiaryDate(),
                diary.getMoodScore(),
                diary.getDominantEmotion(),
                diary.getEmotionTriggers(),
                diary.getDiaryContent(),
                diary.getSleepQuality(),
                diary.getStressLevel(),
                null,
                null,
                false,
                diary.getAnalysisStatus().name(),
                diary.getAiAnalysisUpdatedAt(),
                diary.getAiEmotionAnalysis(),
                diary.getDiaryContent() == null ? 0 : diary.getDiaryContent().length()
        );
    }
}
