package com.n26;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(RecorderProperties.class)
public class ApplicationConfig {

    @Bean
    ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(1);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    TransactionStatisticsRecorder transactionStatistics(ScheduledExecutorService scheduledExecutorService,
                                                        Clock clock,
                                                        RecorderProperties properties) {

        TransactionStatisticsRecorderImpl statistics = new TransactionStatisticsRecorderImpl(
                properties.getDuration(), properties.getResolution(), clock);
        long tickRateNanos = properties.getDuration().dividedBy(properties.getResolution()).toNanos();
        scheduledExecutorService.scheduleAtFixedRate(statistics::tick, tickRateNanos, tickRateNanos, TimeUnit.NANOSECONDS);
        return statistics;
    }

    @Bean
    Duration transactionMaxAge(RecorderProperties properties) {
        return properties.getDuration();
    }

}
