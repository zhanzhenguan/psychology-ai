package com.zzg.psychologyai.interfaces.controller;

import com.zzg.psychologyai.application.query.AnalyticsQueryService;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView;
import com.zzg.psychologyai.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据分析接口
 * <p>
 * 路径前缀：/api/data-analytics（context-path=/api）
 */
@RestController
@RequestMapping("/data-analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    /**
     * 数据分析总览
     * <p>
     * 路径：GET /api/data-analytics/overview
     * 入参：无
     * 返回：{@link AnalyticsOverviewView}，含 systemOverview（系统总览）、
     * emotionHeatmap（7×24 情绪热力图）、emotionTrend（近 30 天情绪趋势）、
     * consultationStats（会话统计）、userActivity（近 30 天用户活跃）
     */
    @GetMapping("/overview")
    public Result<AnalyticsOverviewView> overview() {
        AnalyticsOverviewView overview = analyticsQueryService.overview();
        return Result.success(overview);
    }
}
