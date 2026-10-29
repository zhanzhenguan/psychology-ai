package com.zzg.psychologyai.infrastructure.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzg.psychologyai.domain.model.EmotionDiary;
import com.zzg.psychologyai.domain.port.EmotionAnalysisPort;
import com.zzg.psychologyai.infrastructure.ai.result.EmotionAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 情绪日记 AI 分析服务（领域 EmotionAnalysisPort 的适配实现）：
 * 把用户提交的 6 个字段拼成 prompt，调大模型输出结构化情绪分析 JSON。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionDiaryAiAnalyzer implements EmotionAnalysisPort {

    private static final String[] MOOD_LABELS = {
            "绝望崩溃", "消沉抑郁", "焦虑烦躁", "低落不悦", "平静淡然",
            "轻松惬意", "愉悦舒心", "欢欣满足", "兴奋欣喜", "极致幸福"
    };
    private static final String[] SLEEP_LABELS = {"很差", "较差", "一般", "良好", "优秀"};
    private static final String[] STRESS_LABELS = {"很低", "较低", "中等", "较高", "很高"};
    private static final List<String> EMOTION_CANDIDATES = List.of(
            "快乐", "平静", "兴奋", "满足", "愤怒", "悲伤", "焦虑", "恐惧", "沮丧", "压力"
    );

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * 领域端口入口：以情绪日记聚合为入参，返回可落库的分析结果 JSON
     */
    @Override
    public Optional<String> analyze(EmotionDiary diary) {
        EmotionAnalysisResult result = analyze(
                diary.getMoodScore(),
                diary.getDominantEmotion(),
                diary.getEmotionTriggers(),
                diary.getDiaryContent(),
                diary.getSleepQuality(),
                diary.getStressLevel(),
                System.currentTimeMillis()
        );
        if (result == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            log.error("情绪分析结果序列化失败, diaryId={}", diary.getId(), e);
            return Optional.empty();
        }
    }

    public EmotionAnalysisResult analyze(Integer moodScore,
                                        String dominantEmotion,
                                        String emotionTriggers,
                                        String diaryContent,
                                        Integer sleepQuality,
                                        Integer stressLevel,
                                         Long timestamp) {
        String prompt = buildPrompt(moodScore, dominantEmotion, emotionTriggers,
                diaryContent, sleepQuality, stressLevel,timestamp);
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            String json = extractJson(response);
            return objectMapper.readValue(json, EmotionAnalysisResult.class);
        } catch (Exception e) {
            log.error("AI 分析情绪日记失败", e);
            return null;
        }
    }

    private String buildPrompt(Integer moodScore,
                               String dominantEmotion,
                               String emotionTriggers,
                              String diaryContent,
                               Integer sleepQuality,
                               Integer stressLevel,
                               Long timestamp) {
        return """
                你是情绪分析助手，分析以下用户情绪日记，严格只输出JSON，不要多余文字、解释。
                情绪只能从这个列表选取：%s

                用户日记信息：
                - 心情评分：%d分（%s）
                - 主导情绪：%s
                - 情绪触发因素：%s
                - 日记内容：%s
                - 睡眠质量：%d级（%s）
                - 压力水平：%d级（%s）
                输出JSON字段要求：
                {
                    "primaryEmotion": "主要情绪",
                    "emotionScore": 40,
                    "isNegative": true,
                    "riskLevel": 1,
                    "keywords": ["关键词"],
                    "suggestion": "简短建议",
                    "icon": "😔",
                    "label": "情绪标签",
                    "riskDescription": "风险描述",
                    "improvementSuggestions": ["建议1", "建议2", "建议3"],
                    "timestamp": %d
                }
                """.formatted(EMOTION_CANDIDATES,
                moodScore,
                labelOf(MOOD_LABELS, moodScore),
                dominantEmotion,
                emotionTriggers,
                diaryContent,
                sleepQuality,
                labelOf(SLEEP_LABELS, sleepQuality),
                stressLevel,
                labelOf(STRESS_LABELS, stressLevel),
                timestamp
        );
    }

    private String labelOf(String[] labels, Integer score) {
        if (score == null || score < 1 || score > labels.length) {
            return "未知";
        }
        return labels[score - 1];
    }

    private String extractJson(String text) {
        if (text == null) {
            return "{}";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
