package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日记 PO（对应 emotion_diary 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("emotion_diary")
public class EmotionDiaryPO extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

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



}
