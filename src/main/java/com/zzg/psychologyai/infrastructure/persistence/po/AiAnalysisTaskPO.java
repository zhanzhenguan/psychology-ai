package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI分析任务持久化对象：对应 docs/ai-agent.sql 的 ai_analysis_task 表。
 * <p>
 * 审计字段继承自 BaseEntity；status/taskType/priority 为业务码值，直接以 Integer 承载，
 * 语义见数据库列注释（status：0待处理 1处理中 2已完成 3失败 4已取消）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_analysis_task")
public class AiAnalysisTaskPO extends BaseEntity {

    /**
     * 任务ID（自增主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联日记ID（emotion_diary 表外键）
     */
    private Long diaryId;

    /**
     * 用户ID（user 表外键）
     */
    private Long userId;

    /**
     * 任务状态：0待处理 1处理中 2已完成 3失败 4已取消
     */
    private Integer status;

    /**
     * 任务类型：1情绪分析 2摘要生成 3建议推荐 4综合洞察
     */
    private Integer taskType;

    /**
     * 优先级：1最高 5普通 10最低
     */
    private Integer priority;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 失败错误信息（含异常堆栈摘要）。
     * updateStrategy=ALWAYS：任务完成后需显式清空，null 也写入
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String errorMessage;

    /**
     * 分析结果（JSON 字符串）。
     * updateStrategy=ALWAYS：null 也写入，避免更新语义依赖非空判断
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String result;

    /**
     * 开始执行时间
     */
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
