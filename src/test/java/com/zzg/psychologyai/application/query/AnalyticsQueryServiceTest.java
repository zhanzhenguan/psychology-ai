package com.zzg.psychologyai.application.query;

import com.zzg.psychologyai.application.view.AnalyticsOverviewView;
import com.zzg.psychologyai.infrastructure.persistence.mapper.AnalyticsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * {@link AnalyticsQueryService} 单元测试：纯 Mockito，不启动 Spring、不连库。
 * 重点验证契约结构（7×24 网格、30 天填充）、聚合口径（主导情绪、正负比例、窗口求和）
 * 与异步编排的提交行为。
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsQueryServiceTest {

    @Mock
    private AnalyticsMapper analyticsMapper;

    private AnalyticsQueryService analyticsQueryService;

    @BeforeEach
    void setUp() {
        // 同步执行器：任务在调用线程内直接执行，保证单测结果确定且无需等待线程
        analyticsQueryService = new AnalyticsQueryService(analyticsMapper, Runnable::run);

        // 默认全部空数据，各用例按需覆盖
        lenient().when(analyticsMapper.countUsers()).thenReturn(0L);
        lenient().when(analyticsMapper.countActiveUsers(any())).thenReturn(0L);
        lenient().when(analyticsMapper.countDiaries()).thenReturn(0L);
        lenient().when(analyticsMapper.countSessions()).thenReturn(0L);
        lenient().when(analyticsMapper.avgMoodScore()).thenReturn(0.0);
        lenient().when(analyticsMapper.countTodayUsers()).thenReturn(0L);
        lenient().when(analyticsMapper.countTodayDiaries()).thenReturn(0L);
        lenient().when(analyticsMapper.countTodaySessions()).thenReturn(0L);
        lenient().when(analyticsMapper.selectEmotionHeatmapRows(any())).thenReturn(List.of());
        lenient().when(analyticsMapper.selectEmotionTrendRows(any())).thenReturn(List.of());
        lenient().when(analyticsMapper.selectSessionDailyRows(any())).thenReturn(List.of());
        lenient().when(analyticsMapper.avgSessionDurationMinutes()).thenReturn(0.0);
        lenient().when(analyticsMapper.selectNewUserDailyRows(any())).thenReturn(List.of());
        lenient().when(analyticsMapper.selectActiveUserDailyRows(any())).thenReturn(List.of());
        lenient().when(analyticsMapper.selectDiaryUserDailyRows(any())).thenReturn(List.of());
        lenient().when(analyticsMapper.selectConsultationUserDailyRows(any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("空数据：返回 7×24 默认网格 + 30 天占位趋势/活跃，结构与契约一致")
    void shouldReturnZeroFilledContractWhenNoData() {
        AnalyticsOverviewView view = analyticsQueryService.overview();

        // 网格：7 行 × 24 列，空单元格默认 平静
        assertThat(view.emotionHeatmap().gridData()).hasSize(7);
        view.emotionHeatmap().gridData().forEach(row -> assertThat(row).hasSize(24));
        AnalyticsOverviewView.HeatmapCell cell = view.emotionHeatmap().gridData().get(0).get(0);
        assertThat(cell.x()).isZero();
        assertThat(cell.y()).isZero();
        assertThat(cell.value()).isZero();
        assertThat(cell.dominantEmotion()).isEqualTo("平静");
        assertThat(view.emotionHeatmap().peakEmotionTime()).isEqualTo("00:00");

        // 趋势与用户活跃：各 30 天，坐标连续
        LocalDate start = LocalDate.now().minusDays(29);
        assertThat(view.emotionTrend()).hasSize(30);
        assertThat(view.emotionTrend().get(0).date()).isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(view.emotionTrend().get(29).date()).isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(view.emotionTrend()).allSatisfy(item -> assertThat(item.dominantEmotion()).isEqualTo("无数据"));
        assertThat(view.userActivity()).hasSize(30);
        assertThat(view.consultationStats().dailyTrend()).hasSize(30);

        // dateRange：开始 至 今天
        assertThat(view.emotionHeatmap().dateRange())
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 至 "
                        + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    @DisplayName("热力图：聚合行落到正确坐标，x=小时、y=星期（0=周日）")
    void shouldMapHeatmapRowToCorrectCell() {
        when(analyticsMapper.selectEmotionHeatmapRows(any())).thenReturn(List.of(
                row("hour", 21, "weekday", 6, "emotion", "疲惫", "cnt", 2L, "avgScore", 4.0)
        ));

        AnalyticsOverviewView.HeatmapCell cell = analyticsQueryService.overview()
                .emotionHeatmap().gridData().get(6).get(21);

        assertThat(cell.x()).isEqualTo(21);
        assertThat(cell.y()).isEqualTo(6);
        assertThat(cell.value()).isEqualTo(2L);
        assertThat(cell.avgMoodScore()).isEqualTo(4.0);
        assertThat(cell.dominantEmotion()).isEqualTo("疲惫");
        assertThat(analyticsQueryService.overview().emotionHeatmap().peakEmotionTime()).isEqualTo("21:00");
    }

    @Test
    @DisplayName("热力图：同单元格多情绪取计数最多者为主导情绪，心情分按计数加权")
    void shouldPickDominantEmotionByMaxCountInSameCell() {
        when(analyticsMapper.selectEmotionHeatmapRows(any())).thenReturn(List.of(
                row("hour", 8, "weekday", 1, "emotion", "开心", "cnt", 3L, "avgScore", 5.0),
                row("hour", 8, "weekday", 1, "emotion", "焦虑", "cnt", 1L, "avgScore", 2.0)
        ));

        AnalyticsOverviewView.HeatmapCell cell = analyticsQueryService.overview()
                .emotionHeatmap().gridData().get(1).get(8);

        assertThat(cell.value()).isEqualTo(4L);
        assertThat(cell.dominantEmotion()).isEqualTo("开心");
        // 加权平均：(5*3 + 2*1) / 4 = 4.3（保留 1 位小数）
        assertThat(cell.avgMoodScore()).isEqualTo(4.3);
    }

    @Test
    @DisplayName("情绪趋势：负面情绪计入 negativeRatio，正面计入 positiveRatio，主导情绪取最多")
    void shouldComputeRatiosAndDominantForTrend() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(analyticsMapper.selectEmotionTrendRows(any())).thenReturn(List.of(
                row("date", today, "emotion", "疲惫", "cnt", 2L, "avgScore", 3.0),
                row("date", today, "emotion", "开心", "cnt", 1L, "avgScore", 5.0)
        ));

        AnalyticsOverviewView.EmotionTrendItem item = analyticsQueryService.overview()
                .emotionTrend().get(29);

        assertThat(item.recordCount()).isEqualTo(3L);
        assertThat(item.negativeRatio()).isEqualTo(67L);
        assertThat(item.positiveRatio()).isEqualTo(33L);
        assertThat(item.dominantEmotion()).isEqualTo("疲惫");
        // 加权平均：(3*2 + 5*1) / 3 = 3.7
        assertThat(item.avgMoodScore()).isEqualTo(3.7);
    }

    @Test
    @DisplayName("情绪趋势：中性情绪（满足）两个比例均为 0")
    void shouldReturnZeroRatiosForNeutralEmotion() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(analyticsMapper.selectEmotionTrendRows(any())).thenReturn(List.of(
                row("date", today, "emotion", "满足", "cnt", 1L, "avgScore", 5.0)
        ));

        AnalyticsOverviewView.EmotionTrendItem item = analyticsQueryService.overview()
                .emotionTrend().get(29);

        assertThat(item.positiveRatio()).isZero();
        assertThat(item.negativeRatio()).isZero();
        assertThat(item.dominantEmotion()).isEqualTo("满足");
    }

    @Test
    @DisplayName("情绪分布：按日期序累计各情绪总次数，仅含次数 > 0 的情绪")
    void shouldBuildEmotionDistributionFromTrendRows() {
        String day1 = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String day2 = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(analyticsMapper.selectEmotionTrendRows(any())).thenReturn(List.of(
                row("date", day1, "emotion", "疲惫", "cnt", 1L, "avgScore", 4.0),
                row("date", day2, "emotion", "疲惫", "cnt", 2L, "avgScore", 4.0),
                row("date", day2, "emotion", "满足", "cnt", 1L, "avgScore", 5.0)
        ));

        Map<String, Long> distribution = analyticsQueryService.overview()
                .emotionHeatmap().emotionDistribution();

        assertThat(distribution).containsExactly(
                Map.entry("疲惫", 3L),
                Map.entry("满足", 1L)
        );
    }

    @Test
    @DisplayName("会话统计：窗口内求和 + 缺天补 0 + 平均时长取全量口径")
    void shouldSumWindowSessionsAndFillMissingDays() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(analyticsMapper.selectSessionDailyRows(any())).thenReturn(List.of(
                row("date", today, "sessionCount", 24L, "userCount", 10L)
        ));
        when(analyticsMapper.avgSessionDurationMinutes()).thenReturn(12.5);

        AnalyticsOverviewView.ConsultationStats stats = analyticsQueryService.overview().consultationStats();

        assertThat(stats.totalSessions()).isEqualTo(24L);
        assertThat(stats.avgDurationMinutes()).isEqualTo(12.5);
        assertThat(stats.dailyTrend()).hasSize(30);
        assertThat(stats.dailyTrend().get(29).sessionCount()).isEqualTo(24L);
        assertThat(stats.dailyTrend().get(0).sessionCount()).isZero();
    }

    @Test
    @DisplayName("用户活跃：四类计数按天合并，缺天补 0")
    void shouldMergeFourActivitySourcesByDate() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(analyticsMapper.selectActiveUserDailyRows(any())).thenReturn(List.of(row("date", today, "cnt", 3L)));
        when(analyticsMapper.selectNewUserDailyRows(any())).thenReturn(List.of(row("date", today, "cnt", 7L)));
        when(analyticsMapper.selectDiaryUserDailyRows(any())).thenReturn(List.of(row("date", today, "cnt", 1L)));
        when(analyticsMapper.selectConsultationUserDailyRows(any())).thenReturn(List.of(row("date", today, "cnt", 3L)));

        AnalyticsOverviewView.UserActivityItem item = analyticsQueryService.overview().userActivity().get(29);

        assertThat(item.activeUsers()).isEqualTo(3L);
        assertThat(item.newUsers()).isEqualTo(7L);
        assertThat(item.diaryUsers()).isEqualTo(1L);
        assertThat(item.consultationUsers()).isEqualTo(3L);
    }

    @Test
    @DisplayName("系统总览：直透 Mapper 各项计数")
    void shouldPassThroughSystemOverviewCounts() {
        when(analyticsMapper.countUsers()).thenReturn(4702L);
        when(analyticsMapper.countActiveUsers(any())).thenReturn(172L);
        when(analyticsMapper.countDiaries()).thenReturn(2L);
        when(analyticsMapper.countSessions()).thenReturn(27245L);
        when(analyticsMapper.avgMoodScore()).thenReturn(4.5);
        when(analyticsMapper.countTodayUsers()).thenReturn(7L);
        when(analyticsMapper.countTodayDiaries()).thenReturn(1L);
        when(analyticsMapper.countTodaySessions()).thenReturn(24L);

        AnalyticsOverviewView.SystemOverview overview = analyticsQueryService.overview().systemOverview();

        assertThat(overview.totalUsers()).isEqualTo(4702L);
        assertThat(overview.activeUsers()).isEqualTo(172L);
        assertThat(overview.totalDiaries()).isEqualTo(2L);
        assertThat(overview.totalSessions()).isEqualTo(27245L);
        assertThat(overview.avgMoodScore()).isEqualTo(4.5);
        assertThat(overview.todayNewUsers()).isEqualTo(7L);
        assertThat(overview.todayNewDiaries()).isEqualTo(1L);
        assertThat(overview.todayNewSessions()).isEqualTo(24L);
    }

    @Test
    @DisplayName("异步编排：五个统计分组 + 用户活跃内四类来源均提交到执行器，汇合后结构完整")
    void shouldDispatchAllStatisticBranchesToExecutor() {
        AtomicInteger submissions = new AtomicInteger();
        AnalyticsQueryService asyncService = new AnalyticsQueryService(analyticsMapper, task -> {
            submissions.incrementAndGet();
            task.run();
        });

        AnalyticsOverviewView view = asyncService.overview();

        // 5 个顶层分组 + 用户活跃内 4 个来源查询
        assertThat(submissions.get()).isEqualTo(9);
        assertThat(view.emotionHeatmap().gridData()).hasSize(7);
        assertThat(view.emotionTrend()).hasSize(30);
        assertThat(view.userActivity()).hasSize(30);
        assertThat(view.consultationStats().dailyTrend()).hasSize(30);
        assertThat(view.systemOverview()).isNotNull();
    }

    /** 构造聚合行 Map（键值成对传入） */
    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }
}
