package com.zzg.psychologyai.interfaces.controller;

import com.zzg.psychologyai.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据分析概览接口（对齐目标后端响应结构）
 */
@RestController
@RequestMapping("/data-analytics")
public class AnalyticsController {

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> systemOverview = Map.of(
                "totalUsers", 0,
                "activeUsers", 0,
                "totalDiaries", 0,
                "totalSessions", 0,
                "avgMoodScore", 0.0,
                "todayNewUsers", 0,
                "todayNewDiaries", 0,
                "todayNewSessions", 0
        );
        Map<String, Object> data = Map.of(
                "systemOverview", systemOverview,
                "emotionHeatmap", Map.of("gridData", List.of()),
                "trend", List.of()
        );
        return Result.success(data);
    }
}
