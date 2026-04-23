package fun.datamigration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "dataMigrationExecutor")
    public ThreadPoolTaskExecutor dataMigrationExecutor() {
        // 获取CPU核心数，作为线程池配置的参考
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = Math.max(corePoolSize, 10); // 至少10个线程
        int queueCapacity = 1000; // 任务队列容量

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        // 线程名称前缀
        executor.setThreadNamePrefix("data-migration-");
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：当线程池和队列都满了，由提交任务的线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化线程池
        executor.initialize();

        return executor;
    }
}
