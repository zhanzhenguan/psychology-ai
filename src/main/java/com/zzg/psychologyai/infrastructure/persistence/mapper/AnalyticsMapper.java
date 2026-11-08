package com.zzg.psychologyai.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据分析只读 Mapper：聚合统计 SQL 全部在 XML 中，不操作单表 PO。
 * 仅服务读侧（AnalyticsQueryService），结果以 Map 行返回。
 */
public interface AnalyticsMapper {

    /** 用户总数 */
    long countUsers();

    /** 活跃用户总数：近 N 天有会话或日记行为的去重用户数 */
    long countActiveUsers(@Param("since") LocalDateTime since);

    /** 情绪日记总数 */
    long countDiaries();

    /** 会话总数 */
    long countSessions();

    /** 全量日记平均心情分（保留 1 位小数，无数据返回 0） */
    double avgMoodScore();

    /** 今日新增用户数 */
    long countTodayUsers();

    /** 今日新增日记数 */
    long countTodayDiaries();

    /** 今日新增会话数 */
    long countTodaySessions();

    /**
     * 情绪热力图明细行：按 小时 × 星期 × 主导情绪 聚合。
     * 行字段：hour(0-23)、weekday(0=周日..6=周六)、emotion、cnt、avgScore
     */
    List<Map<String, Object>> selectEmotionHeatmapRows(@Param("start") LocalDateTime start);

    /**
     * 情绪趋势明细行：按 日期 × 主导情绪 聚合。
     * 行字段：date(yyyy-MM-dd)、emotion、cnt、avgScore
     */
    List<Map<String, Object>> selectEmotionTrendRows(@Param("startDate") LocalDate startDate);

    /**
     * 会话每日趋势：date、sessionCount、userCount（去重）
     */
    List<Map<String, Object>> selectSessionDailyRows(@Param("start") LocalDateTime start);

    /** 会话平均时长（分钟，1 位小数）：started_at 到最后一条消息时间 */
    double avgSessionDurationMinutes();

    /** 每日新增用户：date、cnt */
    List<Map<String, Object>> selectNewUserDailyRows(@Param("start") LocalDateTime start);

    /** 每日活跃用户（会话或日记任一行为的去重用户数）：date、cnt */
    List<Map<String, Object>> selectActiveUserDailyRows(@Param("start") LocalDateTime start);

    /** 每日写日记用户（去重）：date、cnt */
    List<Map<String, Object>> selectDiaryUserDailyRows(@Param("startDate") LocalDate startDate);

    /** 每日会话用户（去重）：date、cnt */
    List<Map<String, Object>> selectConsultationUserDailyRows(@Param("start") LocalDateTime start);
}
