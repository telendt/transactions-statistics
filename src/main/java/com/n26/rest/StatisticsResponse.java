package com.n26.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.n26.stats.StatisticsSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class StatisticsResponse {
    private final StatisticsSummary<BigDecimal> summary;
    private final int scale;
    private final RoundingMode roundingMode;

    StatisticsResponse(StatisticsSummary<BigDecimal> summary, int scale, RoundingMode roundingMode) {
        this.summary = summary;
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    @JsonIgnore
    private BigDecimal rounded(BiFunction<Integer, RoundingMode, BigDecimal> fun) {
        return getCount() > 0 ? fun.apply(scale, roundingMode) : BigDecimal.ZERO.setScale(scale, roundingMode);
    }

    @JsonIgnore
    private BigDecimal rounded(Supplier<BigDecimal> supplier) {
        return rounded((s, r) -> supplier.get().setScale(s, r));
    }

    @JsonProperty("sum")
    public BigDecimal getSum() {
        return rounded(summary::getSum);
    }

    @JsonProperty("avg")
    public BigDecimal getAvg() {
        return rounded((s, r) -> getSum().divide(BigDecimal.valueOf(getCount()), s, r));
    }

    @JsonProperty("max")
    public BigDecimal getMax() {
        return rounded(summary::getMax);
    }

    @JsonProperty("min")
    public BigDecimal getMin() {
        return rounded(summary::getMin);
    }

    @JsonProperty("count")
    public long getCount() {
        return summary.getCount();
    }
}