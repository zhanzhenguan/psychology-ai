package com.zzg.psychologyai.application.dto;

/**
 * @author: zzg
 * @date: 2026年10月18日  12:27
 * @version: 1.0
 */

import java.io.Serializable;
import java.util.List;

/**
 * 情绪分析返回Data DTO
 */
public record EmotionAnalyzeDTO(
        /**
         * 主要情绪
         */
        String primaryEmotion,
        /**
         * 情绪分数
         */
        Integer emotionScore,
        /**
         * 是否为负面情绪
         */
        Boolean isNegative,
        /**
         * 风险等级
         */
        Integer riskLevel,
        /**
         * 关键词
         */
        List<String> keywords,
        /**
         * 建议
         */
        String suggestion,
        /**
         * 图标
         */
        String icon,
        /**
         * 标签
         */
        String label,
        /**
         * 风险描述
         */
        String riskDescription,
        /**
         * 改进建议
         */
        List<String> improvementSuggestions,
        /**
         * 时间戳
         */
        Long timestamp
) implements Serializable {

    public EmotionAnalyzeDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }



}
