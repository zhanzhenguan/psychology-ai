package com.zzg.psychologyai.interfaces.response;

import com.zzg.psychologyai.application.dto.EmotionAnalyzeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: zzg
 * @date: 2026年10月18日  11:34
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmotionResponse {

    /**
     * 主要情绪
     */
    private String primaryEmotion;

    /**
     * 情绪分数
     */
    private Integer emotionScore;

    /**
     * 是否负面情绪
     */
    private Boolean isNegative;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 关键词列表
     */
    private List<String> keywords;

    /**
     * 建议文案
     */
    private String suggestion;

    /**
     * 图标unicode
     */
    private String icon;

    /**
     * 标签编码
     */
    private String label;

    /**
     * 风险描述
     */
    private String riskDescription;

    /**
     * 改善建议列表
     */
    private List<String> improvementSuggestions;

    /**
     * 时间戳
     */
    private Long timestamp;


    public static EmotionResponse from(EmotionAnalyzeDTO emotionAnalyzeDTO) {
        return  EmotionResponse.builder()
                .primaryEmotion(emotionAnalyzeDTO.primaryEmotion())
                .emotionScore(emotionAnalyzeDTO.emotionScore())
                .isNegative(emotionAnalyzeDTO.isNegative())
                .riskLevel(emotionAnalyzeDTO.riskLevel())
                .keywords(emotionAnalyzeDTO.keywords())
                .suggestion(emotionAnalyzeDTO.suggestion())
                .icon(emotionAnalyzeDTO.icon())
                .label(emotionAnalyzeDTO.label())
                .riskDescription(emotionAnalyzeDTO.riskDescription())
                .improvementSuggestions(emotionAnalyzeDTO.improvementSuggestions())
                .timestamp(emotionAnalyzeDTO.timestamp())
                .build();
    }
}
