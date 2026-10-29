package com.zzg.psychologyai.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 情绪日记 AI 分析状态枚举（对应 emotion_diary.ai_analysis_status 列码值）
 */
public enum AnalysisStatusEnum {

    /** 待分析 */
    PENDING("PENDING", "待分析"),
    /** 已分析 */
    COMPLETED("COMPLETED", "已分析"),
    /** 分析失败 */
    FAILED("FAILED", "分析失败");

    @EnumValue
    private final String value;

    private final String label;

    AnalysisStatusEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static AnalysisStatusEnum of(String value) {
        if (value == null) {
            return PENDING;
        }
        for (AnalysisStatusEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return PENDING;
    }
}
