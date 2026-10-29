package com.zzg.psychologyai.domain.enums;

/**
 * AI 分析任务状态枚举（对应 ai_analysis_task.status 列码值）
 */
public enum AnalysisTaskStatusEnum {

    /** 待处理 */
    PENDING(0, "待处理"),
    /** 处理中 */
    PROCESSING(1, "处理中"),
    /** 已完成 */
    COMPLETED(2, "已完成"),
    /** 失败（重试耗尽后的终态） */
    FAILED(3, "失败"),
    /** 已取消 */
    CANCELLED(4, "已取消");

    private final Integer code;

    private final String label;

    AnalysisTaskStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AnalysisTaskStatusEnum of(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (AnalysisTaskStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return PENDING;
    }
}
