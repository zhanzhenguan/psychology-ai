package com.zzg.psychologyai.infrastructure.async.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步编排线程池配置
 * <p>
 * 提供一个专用的异步编排线程池，供 application 层用
 * {@code CompletableFuture} 并行编排互不依赖的统计分支（如数据分析总览的多个聚合查询）。
 * <p>
 * 说明：Spring Boot 的 {@code TaskExecutionAutoConfiguration} 仅在容器缺少 Executor Bean 时
 * 才会创建默认的 {@code applicationTaskExecutor}，这里显式定义同名线程池接管该角色；
 * 容器内还存在 {@code taskScheduler}（同为 Executor），故 application 层需按 Bean 名
 * {@code applicationTaskExecutor} 注入，避免类型歧义。
 */
@Configuration
public class AsyncExecutorConfig {

    /** 最坏情况并发分支数：5 个顶层统计分组 + 用户活跃分组内 4 个来源查询 */
    private static final int PARALLEL_BRANCHES = 5 + 4;
    /** 核心线程数：覆盖单次请求的全部并行分支，避免分支排队 */
    private static final int CORE_POOL_SIZE = PARALLEL_BRANCHES;
    /** 最大线程数：并发请求增多时按需扩容 */
    private static final int MAX_POOL_SIZE = PARALLEL_BRANCHES * 2;
    /** 等待队列容量 */
    private static final int QUEUE_CAPACITY = 128;
    /** 非核心线程空闲存活秒数 */
    private static final int KEEP_ALIVE_SECONDS = 60;
    /** 关闭时等待在途任务完成的秒数 */
    private static final int AWAIT_TERMINATION_SECONDS = 30;
    private static final String THREAD_NAME_PREFIX = "async-orchestration-";

    /**
     * 异步编排线程池
     * <p>
     * 队列满时采用 {@link ThreadPoolExecutor.CallerRunsPolicy}，由调用线程兜底执行，
     * 保证统计请求不会因线程池饱和而直接失败。
     *
     * @return 线程池执行器
     */
    @Bean
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        return executor;
    }

    /**
     * MDC 任务装饰器：把提交线程的 MDC 上下文（traceId）透传到工作线程，
     * 使异步分支内打印的日志仍能与请求链路串联。
     */
    private static final class MdcTaskDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        }
    }
}
