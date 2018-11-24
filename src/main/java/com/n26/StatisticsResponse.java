package com.n26;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;

public class StatisticsResponse {
    static final StatisticsResponse ZERO_VALUE = new StatisticsResponse(null, null, null, null, 0);

    private final BigDecimal sum;
    private final BigDecimal avg;
    private final BigDecimal max;
    private final BigDecimal min;
    private final long count;

    StatisticsResponse(@Nullable BigDecimal sum, @Nullable BigDecimal avg, @Nullable BigDecimal max,
                       @Nullable BigDecimal min, long count) {
        this.sum = sum;
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.count = count;
    }

    @Nullable
    public BigDecimal getSum() {
        return sum;
    }

    @Nullable
    public BigDecimal getAvg() {
        return avg;
    }

    @Nullable
    public BigDecimal getMax() {
        return max;
    }

    @Nullable
    public BigDecimal getMin() {
        return min;
    }

    public long getCount() {
        return count;
    }
}