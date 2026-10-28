package com.zzg.psychologyai.infrastructure.persistence.read;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author: zzg
 * @date: 2026年10月19日  00:49
 * @version: 1.0
 */

@Data
public class EmotionDiaryDO {
    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private LocalDate diaryDate;

    private Integer moodScore;

    private String dominantEmotion;

    private String emotionTriggers;

    private String diaryContent;

    private Integer sleepQuality;

    private Integer stressLevel;

    /**
     * AI分析状态：PENDING 待分析， COMPLETED 已分析
     */
    private String aiAnalysisStatus;

    /** AI情绪分析结果（JSON 字符串） */
    private String aiEmotionAnalysis;

    /** AI分析更新时间 */
    private LocalDateTime aiAnalysisUpdatedAt;
    
    
    private Integer contentLength;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean hasAiEmotionAnalysis;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    
    


}
