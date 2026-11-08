package com.zzg.psychologyai.interfaces.controller;

import com.zzg.psychologyai.application.query.AnalyticsQueryService;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView;
import com.zzg.psychologyai.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AnalyticsController} 接口测试：standalone MockMvc，不启动 Spring 容器。
 */
class AnalyticsControllerTest {

    private final AnalyticsQueryService analyticsQueryService = mock(AnalyticsQueryService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AnalyticsController(analyticsQueryService))
            .build();

    @Test
    @DisplayName("GET /data-analytics/overview：返回统一响应与契约五大块结构")
    void shouldReturnOverviewWithContractStructure() throws Exception {
        AnalyticsOverviewView view = new AnalyticsOverviewView(
                new AnalyticsOverviewView.SystemOverview(10L, 2L, 3L, 4L, 4.5, 1L, 1L, 1L),
                new AnalyticsOverviewView.EmotionHeatmap(List.of(), Map.of("开心", 1L), "00:00", "2026-10-10 至 2026-11-08"),
                List.of(new AnalyticsOverviewView.EmotionTrendItem("2026-11-08", 5.0, 1L, 0L, 0L, "满足")),
                new AnalyticsOverviewView.ConsultationStats(4L, 12.5,
                        List.of(new AnalyticsOverviewView.SessionDailyItem("2026-11-08", 4L, 2L))),
                List.of(new AnalyticsOverviewView.UserActivityItem("2026-11-08", 3L, 1L, 1L, 3L))
        );
        when(analyticsQueryService.overview()).thenReturn(view);

        mockMvc.perform(get("/data-analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success(null).getCode()))
                .andExpect(jsonPath("$.data.systemOverview.totalUsers").value(10))
                .andExpect(jsonPath("$.data.systemOverview.avgMoodScore").value(4.5))
                .andExpect(jsonPath("$.data.emotionHeatmap.peakEmotionTime").value("00:00"))
                .andExpect(jsonPath("$.data.emotionHeatmap.dateRange").value("2026-10-10 至 2026-11-08"))
                .andExpect(jsonPath("$.data.emotionTrend[0].dominantEmotion").value("满足"))
                .andExpect(jsonPath("$.data.consultationStats.dailyTrend[0].sessionCount").value(4))
                .andExpect(jsonPath("$.data.userActivity[0].consultationUsers").value(3));
    }
}
