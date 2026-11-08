package com.zzg.psychologyai.application.query;

import com.zzg.psychologyai.application.view.AnalyticsOverviewView;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.ConsultationStats;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.EmotionHeatmap;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.EmotionTrendItem;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.HeatmapCell;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.SessionDailyItem;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.SystemOverview;
import com.zzg.psychologyai.application.view.AnalyticsOverviewView.UserActivityItem;
import com.zzg.psychologyai.infrastructure.persistence.mapper.AnalyticsMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * 数据分析查询服务（CQRS 读侧）：
 * 直接走 {@link AnalyticsMapper} 聚合 SQL，组装为契约读模型，不加载聚合根。
 * 统计窗口固定为近 30 天（含今天）。
 * <p>
 * 各统计分组相互独立，通过 {@link CompletableFuture} 提交到线程池并行编排，缩短接口响应时间。
 * 并行分支各自从连接池取连接，跨线程事务无法传播，故本类不声明 {@code @Transactional}，
 * 避免主线程空占一个连接却无法覆盖分支查询。
 */
@Service
public class AnalyticsQueryService {

    /** 统计窗口天数（近 30 天，含今天） */
    private static final int RANGE_DAYS = 30;
    /** 热力图网格尺寸：24 小时 × 7 天 */
    private static final int HOURS_PER_DAY = 24;
    private static final int DAYS_PER_WEEK = 7;
    private static final String DEFAULT_EMOTION = "平静";
    private static final String NO_DATA_EMOTION = "无数据";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 负面情绪集合：命中计入 negativeRatio；
     * 未命中且不在正面集合中的情绪（如 满足/平静/无数据）视为中性，两个比例均不计。
     */
    private static final Set<String> NEGATIVE_EMOTIONS = Set.of(
            "疲惫", "焦虑", "难过", "愤怒", "压抑", "沮丧", "孤独", "烦躁", "紧张", "恐惧", "悲伤", "郁闷", "委屈", "失望", "担心");
    /** 正面情绪集合：命中计入 positiveRatio */
    private static final Set<String> POSITIVE_EMOTIONS = Set.of(
            "开心", "快乐", "兴奋", "愉悦", "幸福", "放松", "惊喜", "感动", "安心", "期待");

    private final AnalyticsMapper analyticsMapper;
    /** 异步编排执行器（由 infrastructure 层线程池提供，按 Bean 名注入避免与 taskScheduler 冲突） */
    private final Executor executor;

    /**
     * @param analyticsMapper 聚合查询 Mapper
     * @param executor        异步编排执行器
     */
    public AnalyticsQueryService(AnalyticsMapper analyticsMapper,
                                 @Qualifier("applicationTaskExecutor") Executor executor) {
        this.analyticsMapper = analyticsMapper;
        this.executor = executor;
    }

    /**
     * 数据分析总览：系统总览 + 情绪热力图 + 情绪趋势 + 会话统计 + 用户活跃
     * <p>
     * 五个统计分组之间无数据依赖，全部提交到线程池并行执行后再统一汇合组装，
     * 接口耗时由「各分组查询耗时之和」降为「最慢分组的耗时」。
     *
     * @return 概览读模型，结构对齐 DEV.MD 契约
     */
    public AnalyticsOverviewView overview() {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(RANGE_DAYS - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();

        // 情绪明细只查一次：热力图分布与情绪趋势共用同一份聚合结果，避免重复 SQL
        CompletableFuture<List<Map<String, Object>>> heatmapRowsFuture =
                CompletableFuture.supplyAsync(() -> analyticsMapper.selectEmotionHeatmapRows(startTime), executor);
        CompletableFuture<List<Map<String, Object>>> trendRowsFuture =
                CompletableFuture.supplyAsync(() -> analyticsMapper.selectEmotionTrendRows(startDate), executor);

        CompletableFuture<SystemOverview> systemFuture = CompletableFuture.supplyAsync(
                () -> buildSystemOverview(startTime), executor);
        CompletableFuture<EmotionHeatmap> heatmapFuture = heatmapRowsFuture.thenCombine(trendRowsFuture,
                (heatmapRows, trendRows) -> buildEmotionHeatmap(startDate, today, heatmapRows, trendRows));
        CompletableFuture<List<EmotionTrendItem>> trendFuture = trendRowsFuture.thenApply(
                trendRows -> buildEmotionTrend(startDate, today, trendRows));
        CompletableFuture<ConsultationStats> consultationFuture = CompletableFuture.supplyAsync(
                () -> buildConsultationStats(startTime, startDate, today), executor);
        CompletableFuture<List<UserActivityItem>> activityFuture = CompletableFuture.supplyAsync(
                () -> buildUserActivity(startTime, startDate, today), executor);

        return new AnalyticsOverviewView(
                await(systemFuture),
                await(heatmapFuture),
                await(trendFuture),
                await(consultationFuture),
                await(activityFuture));
    }

    /** 系统总览：全量计数 + 今日新增 + 近 30 天活跃用户 */
    private SystemOverview buildSystemOverview(LocalDateTime activeSince) {
        return new SystemOverview(
                analyticsMapper.countUsers(),
                analyticsMapper.countActiveUsers(activeSince),
                analyticsMapper.countDiaries(),
                analyticsMapper.countSessions(),
                analyticsMapper.avgMoodScore(),
                analyticsMapper.countTodayUsers(),
                analyticsMapper.countTodayDiaries(),
                analyticsMapper.countTodaySessions()
        );
    }

    /**
     * 情绪热力图：把 小时×星期×情绪 聚合行填充进 7×24 网格；
     * 空单元格默认 value=0、avgMoodScore=0、dominantEmotion=「平静」。
     *
     * @param heatmapRows 热力图明细（小时×星期×情绪）
     * @param trendRows   情绪趋势明细（日期×情绪），复用于情绪分布，避免重复查询
     */
    private EmotionHeatmap buildEmotionHeatmap(LocalDate startDate, LocalDate today,
                                               List<Map<String, Object>> heatmapRows,
                                               List<Map<String, Object>> trendRows) {
        // 网格直接以下标定位，省去 Map key 运算
        CellBucket[][] buckets = new CellBucket[DAYS_PER_WEEK][HOURS_PER_DAY];
        // 各小时总记录数（用于峰值时段）
        long[] hourTotals = new long[HOURS_PER_DAY];
        for (Map<String, Object> row : heatmapRows) {
            int hour = asInt(row.get("hour"));
            int weekday = asInt(row.get("weekday"));
            if (weekday < 0 || weekday >= DAYS_PER_WEEK || hour < 0 || hour >= HOURS_PER_DAY) {
                continue;
            }
            CellBucket bucket = buckets[weekday][hour];
            if (bucket == null) {
                bucket = new CellBucket();
                buckets[weekday][hour] = bucket;
            }
            bucket.add(asString(row.get("emotion")), asLong(row.get("cnt")), asDouble(row.get("avgScore")));
            hourTotals[hour] += asLong(row.get("cnt"));
        }

        List<List<HeatmapCell>> grid = new ArrayList<>(DAYS_PER_WEEK);
        for (int y = 0; y < DAYS_PER_WEEK; y++) {
            List<HeatmapCell> row = new ArrayList<>(HOURS_PER_DAY);
            for (int x = 0; x < HOURS_PER_DAY; x++) {
                CellBucket bucket = buckets[y][x];
                row.add(bucket == null
                        ? new HeatmapCell(x, y, 0L, 0.0, DEFAULT_EMOTION)
                        : bucket.toCell(x, y));
            }
            grid.add(row);
        }

        // 情绪分布：按趋势行（按日期升序）累计，保证键顺序按首次出现时间
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Map<String, Object> trendRow : trendRows) {
            distribution.merge(asString(trendRow.get("emotion")), asLong(trendRow.get("cnt")), Long::sum);
        }

        // 峰值时段：总记录数最多的小时，并列取最小小时；全窗口无数据时默认 00:00
        int peakHour = 0;
        long peakCnt = 0L;
        for (int h = 0; h < HOURS_PER_DAY; h++) {
            if (hourTotals[h] > peakCnt) {
                peakCnt = hourTotals[h];
                peakHour = h;
            }
        }

        return new EmotionHeatmap(
                grid,
                distribution,
                String.format("%02d:00", peakHour),
                startDate.format(DATE_FMT) + " 至 " + today.format(DATE_FMT)
        );
    }

    /**
     * 情绪趋势：按天聚合，窗口内每一天都占位；无数据天 dominantEmotion=「无数据」。
     *
     * @param trendRows 情绪趋势明细（日期×情绪，与热力图分布共用）
     */
    private List<EmotionTrendItem> buildEmotionTrend(LocalDate startDate, LocalDate today,
                                                     List<Map<String, Object>> trendRows) {
        // 按日期分桶：date -> 该日各情绪计数 / 心情分加权
        Map<String, DayBucket> dayBuckets = new LinkedHashMap<>();
        for (Map<String, Object> row : trendRows) {
            dayBuckets.computeIfAbsent(asString(row.get("date")), k -> new DayBucket())
                    .add(asString(row.get("emotion")), asLong(row.get("cnt")), asDouble(row.get("avgScore")));
        }

        List<EmotionTrendItem> trend = new ArrayList<>(RANGE_DAYS);
        for (String date : dateRange(startDate, today)) {
            DayBucket bucket = dayBuckets.get(date);
            trend.add(bucket == null
                    ? new EmotionTrendItem(date, 0.0, 0L, 0L, 0L, NO_DATA_EMOTION)
                    : bucket.toTrendItem(date));
        }
        return trend;
    }

    /** 会话统计：窗口内会话总数 + 全量平均时长 + 每日趋势（缺天补 0） */
    private ConsultationStats buildConsultationStats(LocalDateTime startTime, LocalDate startDate, LocalDate today) {
        Map<String, Map<String, Object>> byDate = indexByDate(analyticsMapper.selectSessionDailyRows(startTime));
        List<SessionDailyItem> daily = new ArrayList<>(RANGE_DAYS);
        long windowSessions = 0L;
        for (String date : dateRange(startDate, today)) {
            Map<String, Object> row = byDate.get(date);
            long sessionCount = row == null ? 0L : asLong(row.get("sessionCount"));
            long userCount = row == null ? 0L : asLong(row.get("userCount"));
            windowSessions += sessionCount;
            daily.add(new SessionDailyItem(date, sessionCount, userCount));
        }
        return new ConsultationStats(windowSessions, analyticsMapper.avgSessionDurationMinutes(), daily);
    }

    /**
     * 用户活跃：按天合并 活跃/新增/写日记/会话 四类计数（缺天补 0）
     * <p>
     * 四类来源互不依赖，在本分组内二次并行编排。
     */
    private List<UserActivityItem> buildUserActivity(LocalDateTime startTime, LocalDate startDate, LocalDate today) {
        CompletableFuture<List<Map<String, Object>>> activeFuture = CompletableFuture.supplyAsync(
                () -> analyticsMapper.selectActiveUserDailyRows(startTime), executor);
        CompletableFuture<List<Map<String, Object>>> newFuture = CompletableFuture.supplyAsync(
                () -> analyticsMapper.selectNewUserDailyRows(startTime), executor);
        CompletableFuture<List<Map<String, Object>>> diaryFuture = CompletableFuture.supplyAsync(
                () -> analyticsMapper.selectDiaryUserDailyRows(startDate), executor);
        CompletableFuture<List<Map<String, Object>>> consultFuture = CompletableFuture.supplyAsync(
                () -> analyticsMapper.selectConsultationUserDailyRows(startTime), executor);

        Map<String, Long> activeByDate = toLongByDate(await(activeFuture));
        Map<String, Long> newByDate = toLongByDate(await(newFuture));
        Map<String, Long> diaryByDate = toLongByDate(await(diaryFuture));
        Map<String, Long> consultByDate = toLongByDate(await(consultFuture));

        List<UserActivityItem> activity = new ArrayList<>(RANGE_DAYS);
        for (String date : dateRange(startDate, today)) {
            activity.add(new UserActivityItem(
                    date,
                    activeByDate.getOrDefault(date, 0L),
                    newByDate.getOrDefault(date, 0L),
                    diaryByDate.getOrDefault(date, 0L),
                    consultByDate.getOrDefault(date, 0L)
            ));
        }
        return activity;
    }

    /**
     * 汇合并提取异步分支结果：解包 {@link CompletionException}，
     * 还原业务异常类型，便于全局异常处理器按原类型响应。
     *
     * @param future 异步分支
     * @return 分支计算结果
     */
    private static <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("异步统计任务执行失败", cause);
        }
    }

    /** 生成窗口内连续日期字符串（含首尾，按天升序），供趋势/会话/活跃按天补零 */
    private static List<String> dateRange(LocalDate startDate, LocalDate today) {
        List<String> dates = new ArrayList<>(RANGE_DAYS);
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1L)) {
            dates.add(d.format(DATE_FMT));
        }
        return dates;
    }

    private static Map<String, Map<String, Object>> indexByDate(List<Map<String, Object>> rows) {
        Map<String, Map<String, Object>> byDate = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            byDate.put(asString(row.get("date")), row);
        }
        return byDate;
    }

    private static Map<String, Long> toLongByDate(List<Map<String, Object>> rows) {
        Map<String, Long> byDate = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            byDate.put(asString(row.get("date")), asLong(row.get("cnt")));
        }
        return byDate;
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    /** 保留 1 位小数 */
    private static double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 热力图单元格聚合桶：累计记录数、心情分加权求和、各情绪计数。
     * 主导情绪取计数最多者，并列时保留先出现者（LinkedHashMap 迭代序）。
     */
    private static final class CellBucket {
        private long cnt;
        private double scoreSum;
        private final Map<String, Long> emotionCounts = new LinkedHashMap<>();

        void add(String emotion, long addCnt, double avgScore) {
            this.cnt += addCnt;
            this.scoreSum += avgScore * addCnt;
            this.emotionCounts.merge(emotion, addCnt, Long::sum);
        }

        HeatmapCell toCell(int x, int y) {
            return new HeatmapCell(x, y, cnt, round1(scoreSum / cnt), dominantEmotion(emotionCounts, DEFAULT_EMOTION));
        }
    }

    /**
     * 单日情绪聚合桶：累计记录数、心情分加权求和、正/负面情绪计数。
     */
    private static final class DayBucket {
        private long cnt;
        private double scoreSum;
        private long positive;
        private long negative;
        private final Map<String, Long> emotionCounts = new LinkedHashMap<>();

        void add(String emotion, long addCnt, double avgScore) {
            this.cnt += addCnt;
            this.scoreSum += avgScore * addCnt;
            this.emotionCounts.merge(emotion, addCnt, Long::sum);
            if (POSITIVE_EMOTIONS.contains(emotion)) {
                this.positive += addCnt;
            }
            if (NEGATIVE_EMOTIONS.contains(emotion)) {
                this.negative += addCnt;
            }
        }

        EmotionTrendItem toTrendItem(String date) {
            return new EmotionTrendItem(
                    date,
                    round1(scoreSum / cnt),
                    cnt,
                    Math.round(positive * 100.0 / cnt),
                    Math.round(negative * 100.0 / cnt),
                    dominantEmotion(emotionCounts, NO_DATA_EMOTION)
            );
        }
    }

    /** 计数最多的情绪；并列保留先出现者；空 Map 返回默认值 */
    private static String dominantEmotion(Map<String, Long> emotionCounts, String fallback) {
        String dominant = fallback;
        long max = 0L;
        for (Map.Entry<String, Long> entry : emotionCounts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return dominant;
    }
}
