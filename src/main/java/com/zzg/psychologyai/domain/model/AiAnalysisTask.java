package com.zzg.psychologyai.domain.model;

import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.common.exception.domain.DomainException;
import com.zzg.psychologyai.domain.common.model.AggregateRoot;
import com.zzg.psychologyai.domain.enums.AnalysisTaskStatusEnum;
import com.zzg.psychologyai.domain.enums.AnalysisTaskTypeEnum;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI 分析任务聚合根（对应 ai_analysis_task 表）。
 * <p>
 * 任务表是情绪日记 AI 分析流程的驱动表：日记保存时同事务创建任务，
 * 调度器轮询待处理任务提交大模型，再按结果回写任务表与日记表。
 * 状态机：PENDING → PROCESSING → COMPLETED；执行失败累加重试，
 * 未达上限回到 PENDING 等待下轮重试，达到上限进入 FAILED 终态。
 */
public class AiAnalysisTask extends AggregateRoot {

    /** 默认优先级：普通 */
    public static final int DEFAULT_PRIORITY = 5;

    /** 默认最大重试次数 */
    public static final int DEFAULT_MAX_RETRY = 3;

    private Long id;

    /** 关联日记ID */
    private Long diaryId;

    /** 用户ID */
    private Long userId;

    /** 任务状态 */
    private AnalysisTaskStatusEnum status;

    /** 任务类型 */
    private AnalysisTaskTypeEnum taskType;

    /** 优先级：1最高 5普通 10最低 */
    private Integer priority;

    /** 当前重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetryCount;

    /** 失败错误信息 */
    private String errorMessage;

    /** 分析结果（JSON 字符串） */
    private String result;

    /** 开始执行时间 */
    private LocalDateTime startedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    private AiAnalysisTask() {
    }

    /**
     * 创建情绪分析任务（初始：待处理、普通优先级、默认重试上限）
     */
    public static AiAnalysisTask create(Long diaryId, Long userId) {
        AiAnalysisTask task = new AiAnalysisTask();
        task.diaryId = diaryId;
        task.userId = userId;
        task.status = AnalysisTaskStatusEnum.PENDING;
        task.taskType = AnalysisTaskTypeEnum.EMOTION_ANALYSIS;
        task.priority = DEFAULT_PRIORITY;
        task.retryCount = 0;
        task.maxRetryCount = DEFAULT_MAX_RETRY;
        return task;
    }

    /**
     * 从持久化数据还原聚合（仅仓储实现调用）
     */
    public static AiAnalysisTask restore(Long id, Long diaryId, Long userId,
                                         Integer status, Integer taskType,
                                         Integer priority, Integer retryCount, Integer maxRetryCount,
                                         String errorMessage, String result,
                                         LocalDateTime startedAt, LocalDateTime completedAt) {
        AiAnalysisTask task = new AiAnalysisTask();
        task.id = id;
        task.diaryId = diaryId;
        task.userId = userId;
        task.status = AnalysisTaskStatusEnum.of(status);
        task.taskType = AnalysisTaskTypeEnum.of(taskType);
        task.priority = priority;
        task.retryCount = retryCount == null ? 0 : retryCount;
        task.maxRetryCount = maxRetryCount == null ? DEFAULT_MAX_RETRY : maxRetryCount;
        task.errorMessage = errorMessage;
        task.result = result;
        task.startedAt = startedAt;
        task.completedAt = completedAt;
        return task;
    }

    /**
     * 认领任务：待处理 → 处理中
     */
    public void markProcessing() {
        if (status != AnalysisTaskStatusEnum.PENDING) {
            throw new BizException(ResultCode.BAD_REQUEST, "任务当前状态不允许认领"+ id);
        }
        this.status = AnalysisTaskStatusEnum.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 分析成功：回填结构化结果，任务进入完成终态
     */
    public void markCompleted(String analysisResult) {
        this.status = AnalysisTaskStatusEnum.COMPLETED;
        this.result = analysisResult;
        this.errorMessage = null;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 执行失败：重试次数 +1；未达上限回到待处理等待下轮重试，达到上限进入失败终态
     */
    public void markFailed(String error) {
        this.retryCount = this.retryCount + 1;
        this.errorMessage = error;
        if (this.retryCount >= this.maxRetryCount) {
            this.status = AnalysisTaskStatusEnum.FAILED;
            this.completedAt = LocalDateTime.now();
        } else {
            this.status = AnalysisTaskStatusEnum.PENDING;
        }
    }

    /**
     * 是否为失败终态（重试已耗尽，需联动日记置失败）
     */
    public boolean isFinalFailed() {
        return status == AnalysisTaskStatusEnum.FAILED;
    }

    public Long getId() { return id; }
    public Long getDiaryId() { return diaryId; }
    public Long getUserId() { return userId; }
    public AnalysisTaskStatusEnum getStatus() { return status; }
    public AnalysisTaskTypeEnum getTaskType() { return taskType; }
    public Integer getPriority() { return priority; }
    public Integer getRetryCount() { return retryCount; }
    public Integer getMaxRetryCount() { return maxRetryCount; }
    public String getErrorMessage() { return errorMessage; }
    public String getResult() { return result; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiAnalysisTask that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
