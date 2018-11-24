package com.n26;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionStatisticsImplTest {
    private static final Instant timeZero = Instant.ofEpochSecond(1543140098L);
    private TransactionStatisticsImpl transactionStatistics;
    private FakeClock clock;

    @Before
    public void setUp() {
        clock = new FakeClock();
        clock.setInstant(timeZero);
        transactionStatistics = new TransactionStatisticsImpl(Duration.ofSeconds(60), 60, clock);
    }

    @Test
    public void testBasic() {
        transactionStatistics.recordTransaction(BigDecimal.valueOf(1), timeZero.minusMillis(100));
        transactionStatistics.recordTransaction(BigDecimal.valueOf(3), timeZero.minusMillis(58500));

        StatisticsSummary<BigDecimal> summary = transactionStatistics.getSummary();

        assertThat(summary.getCount()).isEqualTo(2);
        assertThat(summary.getSum()).isEqualTo(BigDecimal.valueOf(4));
        assertThat(summary.getMax()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(summary.getMin()).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    public void testTicking() {
        transactionStatistics.recordTransaction(BigDecimal.valueOf(1), timeZero.minusMillis(100));
        transactionStatistics.recordTransaction(BigDecimal.valueOf(3), timeZero.minusMillis(58500));

        clock.setInstant(timeZero.plusSeconds(1));
        transactionStatistics.tick(); // value 3 should fall out

        StatisticsSummary summary = transactionStatistics.getSummary();

        assertThat(summary.getCount()).isEqualTo(1);
        assertThat(summary.getSum()).isEqualTo(BigDecimal.valueOf(1));
        assertThat(summary.getMax()).isEqualTo(BigDecimal.valueOf(1));
        assertThat(summary.getMin()).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    public void testRecordAfterTick() {
        transactionStatistics.recordTransaction(BigDecimal.valueOf(1), timeZero.minusMillis(100));
        transactionStatistics.recordTransaction(BigDecimal.valueOf(3), timeZero.minusMillis(58500));

        clock.setInstant(timeZero.plusSeconds(1));
        transactionStatistics.tick(); // value 3 should fall out

        transactionStatistics.recordTransaction(BigDecimal.valueOf(5), timeZero.minusSeconds(10));

        StatisticsSummary summary = transactionStatistics.getSummary();
        assertThat(summary.getCount()).isEqualTo(2);
        assertThat(summary.getSum()).isEqualTo(BigDecimal.valueOf(6));
        assertThat(summary.getMax()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(summary.getMin()).isEqualTo(BigDecimal.valueOf(1));
    }

    static class FakeClock extends Clock {
        Instant instant;

        @Override
        public ZoneId getZone() {
            return null;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return null;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}