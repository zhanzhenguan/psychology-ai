package com.zzg.psychologyai.infrastructure.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzg.psychologyai.infrastructure.ai.result.EmotionAnalysisResult;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationSessionMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationSessionPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 此刻情绪分析工具：对话中大模型调用，把情绪分析结果入库到 consultation_session。
 *
 * @author zzg
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmotionAnalysisTool {

    private final ConsultationSessionMapper consultationSessionMapper;
    private final ObjectMapper objectMapper;

    @Tool(
            name = "mood_analysis_now",
            description = """
                    你是情绪分析助手，分析用户文本情绪，严格只输出JSON，不要多余文字、解释。
                    情绪只能从这个列表选取：['快乐', '平静', '兴奋', '满足', '愤怒', '悲伤', '焦虑', '恐惧', '沮丧', '压力']
                    输出字段要求：
                    - emotionScore: 0~100整数，分数越高情绪越强，负面情绪一般偏高，疲惫40左右
                    - icon: emoji unicode，例：😔
                    - improvementSuggestions: 改善建议数组，3条以内
                    - isNegative: true/false 是否负面情绪
                    - keywords: 提取关键词，中文，例如：疲惫/焦虑/开心
                    - label: 中文标签，例如：疲惫/焦虑/开心
                    - primaryEmotion: 主要情绪，例如：疲惫/焦虑/开心
                    - riskDescription: 风险描述，0-30个字符
                    - riskLevel: 0~3，0正常，1关注，2预警，3危机
                    - suggestion: 简短建议，0-30个字符
                    """
    )
    public String analyzeEmotion(
            @ToolParam(description = "0~100整数情绪评分") Integer emotionScore,
            @ToolParam(description = "emoji图标，例：😔") String icon,
            @ToolParam(description = "改善建议数组，3条以内") List<String> improvementSuggestions,
            @ToolParam(description = "是否负面情绪") Boolean isNegative,
            @ToolParam(description = "提取的中文关键词") List<String> keywords,
            @ToolParam(description = "中文情绪标签") String label,
            @ToolParam(description = "主要情绪") String primaryEmotion,
            @ToolParam(description = "风险描述，0-30字符") String riskDescription,
            @ToolParam(description = "风险等级 0正常/1关注/2预警/3危机") Integer riskLevel,
            @ToolParam(description = "简短建议，0-30字符") String suggestion,
            ToolContext toolContext
    ) {
        // 从 ToolContext 取额外信息
        Long sessionId = (Long) toolContext.getContext().get("sessionId");
        Long userId = (Long) toolContext.getContext().get("userId");
        try {

            EmotionAnalysisResult result = new EmotionAnalysisResult(
                    primaryEmotion, emotionScore, isNegative, riskLevel,
                    keywords, suggestion, icon, label, riskDescription,
                    improvementSuggestions, System.currentTimeMillis()
            );
            String json = objectMapper.writeValueAsString(result);
            ConsultationSessionPO update = new ConsultationSessionPO();
            update.setId(sessionId);
            update.setLastEmotionAnalysis(json);
            update.setLastEmotionUpdatedAt(LocalDateTime.now());
            update.setUpdatedBy(userId);
            consultationSessionMapper.updateById(update);

            log.info("情绪分析已入库, sessionId={}, result={}", sessionId, json);
            return "情绪分析结果已保存";
        } catch (Exception e) {
            log.error("情绪分析入库失败, sessionId={}", sessionId, e);
            return "情绪分析入库失败：" + e.getMessage();
        }
    }
}
