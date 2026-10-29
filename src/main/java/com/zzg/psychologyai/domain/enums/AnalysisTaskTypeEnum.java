package com.zzg.psychologyai.domain.enums;

/**
 * AI 分析任务类型枚举（对应 ai_analysis_task.task_type 列码值）
 */
public enum AnalysisTaskTypeEnum {

    /** 情绪分析 */
    EMOTION_ANALYSIS(1, "情绪分析"),
    /** 摘要生成 */
    SUMMARY_GENERATION(2, "摘要生成"),
    /** 建议推荐 */
    SUGGESTION_RECOMMENDATION(3, "建议推荐"),
    /** 综合洞察 */
    COMPREHENSIVE_INSIGHT(4, "综合洞察");

    private final Integer code;

    private final String label;

    AnalysisTaskTypeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AnalysisTaskTypeEnum of(Integer code) {
        if (code == null) {
            return EMOTION_ANALYSIS;
        }
        for (AnalysisTaskTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return EMOTION_ANALYSIS;
    }
}
