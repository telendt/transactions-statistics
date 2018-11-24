package com.n26;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application {
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizeJacksonMapper() {
        return c -> c.serializerByType(BigDecimal.class, new JsonSerializer<BigDecimal>() {
            @Override
            public void serialize(BigDecimal value, JsonGenerator jGen, SerializerProvider provider) throws IOException {
                jGen.writeString(value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
        });
    }

    @Bean
    ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(1);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    TransactionStatistics transactionStatistics(Clock clock, ScheduledExecutorService scheduledExecutorService) {
        Duration recordingDuration = Duration.ofSeconds(60);
        int resolution = 100;
        TransactionStatisticsImpl statistics = new TransactionStatisticsImpl(recordingDuration, resolution, clock);
        long tickRateNanos = recordingDuration.dividedBy(resolution).toNanos();
        scheduledExecutorService.scheduleAtFixedRate(statistics::tick, tickRateNanos, tickRateNanos, TimeUnit.NANOSECONDS);
        return statistics;
    }
}
