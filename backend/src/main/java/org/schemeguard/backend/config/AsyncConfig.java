package org.schemeguard.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(UploadProperties.class)
public class AsyncConfig {

    @Bean(name = "csvImportExecutor")
    public Executor csvImportExecutor(UploadProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getMaxParallelImports());
        executor.setMaxPoolSize(properties.getMaxParallelImports());
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("csv-import-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
