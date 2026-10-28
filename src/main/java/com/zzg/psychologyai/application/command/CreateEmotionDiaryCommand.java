package com.zzg.psychologyai.application.command;

import java.time.LocalDate;

/**
 * 创建情绪日记命令
 */
public record CreateEmotionDiaryCommand(
        Long userId,
        LocalDate diaryDate,
        Integer moodScore,
        String dominantEmotion,
        String emotionTriggers,
        String diaryContent,
        Integer sleepQuality,
        Integer stressLevel
) {
}
