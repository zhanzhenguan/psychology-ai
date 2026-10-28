package com.zzg.psychologyai.infrastructure.ai.result;

import java.util.List;

/**
 * 情绪分析结果（对应大模型输出 JSON 结构，序列化后存 last_emotion_analysis）
 */
public record EmotionAnalysisResult(
        String primaryEmotion,
        Integer emotionScore,
        Boolean isNegative,
        Integer riskLevel,
        List<String> keywords,
        String suggestion,
        String icon,
        String label,
        String riskDescription,
        List<String> improvementSuggestions,
        Long timestamp
) {
}
