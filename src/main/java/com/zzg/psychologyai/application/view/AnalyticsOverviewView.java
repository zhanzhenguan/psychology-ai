package com.zzg.psychologyai.application.view;

import java.util.List;
import java.util.Map;

/**
 * 数据分析概览读模型（CQRS 读侧投影）。
 * <p>
 * 字段顺序与 DEV.MD 契约 JSON 保持一致，Jackson 按 record 组件声明顺序序列化。
 *
 * @param systemOverview    系统总览指标
 * @param emotionHeatmap    情绪热力图（7 行星期 × 24 列小时）
 * @param emotionTrend      近 30 天情绪趋势（按天，无数据天也占位）
 * @param consultationStats 会话统计
 * @param userActivity      近 30 天用户活跃（按天）
 */
public record AnalyticsOverviewView(
        SystemOverview systemOverview,
        EmotionHeatmap emotionHeatmap,
        List<EmotionTrendItem> emotionTrend,
        ConsultationStats consultationStats,
        List<UserActivityItem> userActivity
) {

    /**
     * 系统总览指标
     */
    public record SystemOverview(
            long totalUsers,
            long activeUsers,
            long totalDiaries,
            long totalSessions,
            double avgMoodScore,
            long todayNewUsers,
            long todayNewDiaries,
            long todayNewSessions
    ) {
    }

    /**
     * 情绪热力图
     *
     * @param gridData            7×24 网格，外层按下标 y（星期，0=周日）索引，内层按 x（小时 0-23）
     * @param emotionDistribution 窗口内各主导情绪出现次数（仅含次数 &gt; 0 的情绪）
     * @param peakEmotionTime     记录数最多的小时，格式 HH:00
     * @param dateRange           统计窗口，格式 yyyy-MM-dd 至 yyyy-MM-dd
     */
    public record EmotionHeatmap(
            List<List<HeatmapCell>> gridData,
            Map<String, Long> emotionDistribution,
            String peakEmotionTime,
            String dateRange
    ) {
    }

    /**
     * 热力图单元格
     *
     * @param x                小时（0-23）
     * @param y                星期（0=周日 .. 6=周六）
     * @param value            该时段日记条数
     * @param avgMoodScore     该时段平均心情分（无数据为 0）
     * @param dominantEmotion  该时段主导情绪（无数据默认「平静」）
     */
    public record HeatmapCell(
            int x,
            int y,
            long value,
            double avgMoodScore,
            String dominantEmotion
    ) {
    }

    /**
     * 单日情绪趋势
     *
     * @param positiveRatio 正面情绪占比（百分比，0-100 整数）
     * @param negativeRatio 负面情绪占比（百分比，0-100 整数）
     */
    public record EmotionTrendItem(
            String date,
            double avgMoodScore,
            long recordCount,
            long positiveRatio,
            long negativeRatio,
            String dominantEmotion
    ) {
    }

    /**
     * 会话统计
     */
    public record ConsultationStats(
            long totalSessions,
            double avgDurationMinutes,
            List<SessionDailyItem> dailyTrend
    ) {
    }

    /**
     * 单日会话趋势
     */
    public record SessionDailyItem(
            String date,
            long sessionCount,
            long userCount
    ) {
    }

    /**
     * 单日用户活跃
     */
    public record UserActivityItem(
            String date,
            long activeUsers,
            long newUsers,
            long diaryUsers,
            long consultationUsers
    ) {
    }
}
