package com.creditville.notifications.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Chuks on 02/06/2021.
 */
@Configuration
@EnableScheduling
@EnableAsync(proxyTargetClass=true)
public class SchedulerConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean()
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

//    @Bean("schedulePool1")
//    public Executor jobPool() {
//        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
//        exec.setCorePoolSize(1);
//        exec.setMaxPoolSize(1);
//        exec.setQueueCapacity(10);
//        exec.setThreadNamePrefix("first-");
//        exec.initialize();
//        return exec;
//    }
//
//    @Bean("schedulePool2")
//    public Executor jobPool2() {
//        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
//        exec.setCorePoolSize(1);
//        exec.setMaxPoolSize(1);
//        exec.setQueueCapacity(10);
//        exec.setThreadNamePrefix("second-");
//        exec.initialize();
//        return exec;
//    }
//
//    @Bean("schedulePool3")
//    public Executor jobPool3() {
//        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
//        exec.setCorePoolSize(1);
//        exec.setMaxPoolSize(1);
//        exec.setQueueCapacity(10);
//        exec.setThreadNamePrefix("third-");
//        exec.initialize();
//        return exec;
//    }
//
//    @Bean("schedulePool4")
//    public Executor jobPool4() {
//        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
//        exec.setCorePoolSize(1);
//        exec.setMaxPoolSize(1);
//        exec.setQueueCapacity(10);
//        exec.setThreadNamePrefix("fourth-");
//        exec.initialize();
//        return exec;
//    }
//
//    @Bean("schedulePool5")
//    public Executor jobPool5() {
//        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
//        exec.setCorePoolSize(1);
//        exec.setMaxPoolSize(1);
//        exec.setQueueCapacity(10);
//        exec.setThreadNamePrefix("fifth-");
//        exec.initialize();
//        return exec;
//    }
//
//    @Bean("schedulePool6")
//    public Executor jobPool6() {
//        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
//        exec.setCorePoolSize(1);
//        exec.setMaxPoolSize(1);
//        exec.setQueueCapacity(10);
//        exec.setThreadNamePrefix("sixth-");
//        exec.initialize();
//        return exec;
//    }
}
