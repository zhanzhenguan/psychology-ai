package com.zzg.psychologyai.domain.model;

import com.zzg.psychologyai.domain.common.model.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 咨询会话聚合根（对应 consultation_session 表）
 */
public class ConsultationSession extends AggregateRoot {

    private Long id;
    private Long userId;

    /** 会话标题 */
    private String sessionTitle;

    private LocalDateTime startedAt;

    /** 最后一次情绪分析结果（JSON） */
    private String lastEmotionAnalysis;

    private LocalDateTime lastEmotionUpdatedAt;






    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public String getLastEmotionAnalysis() { return lastEmotionAnalysis; }
    public void setLastEmotionAnalysis(String lastEmotionAnalysis) { this.lastEmotionAnalysis = lastEmotionAnalysis; }
    public LocalDateTime getLastEmotionUpdatedAt() { return lastEmotionUpdatedAt; }
    public void setLastEmotionUpdatedAt(LocalDateTime lastEmotionUpdatedAt) { this.lastEmotionUpdatedAt = lastEmotionUpdatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultationSession that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
