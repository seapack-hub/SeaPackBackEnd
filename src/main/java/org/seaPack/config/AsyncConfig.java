package org.seaPack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步任务线程池配置
 * <p>提供统一的 SSE 流式任务执行线程池。</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * SSE 流式任务执行线程池
     * <p>用于所有 AI 对话 SSE 端点的异步执行，避免阻塞 Servlet 容器线程。</p>
     */
    @Bean("sseExecutor")
    public Executor sseExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(Math.max(cores * 2, 16));
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("sse-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
