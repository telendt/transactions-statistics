package com.n26;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class StatisticsResponse {
    private final StatisticsSummary<BigDecimal> summary;

    StatisticsResponse(StatisticsSummary<BigDecimal> summary) {
        this.summary = summary;
    }

    @JsonProperty("sum")
    public BigDecimal getSum() {
        return getCount() > 0 ? summary.getSum() : BigDecimal.ZERO;
    }

    @JsonProperty("avg")
    public BigDecimal getAvg() {
        return getCount() > 0 ?
                getSum().divide(BigDecimal.valueOf(getCount()), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    @JsonProperty("max")
    public BigDecimal getMax() {
        return getCount() > 0 ? summary.getMax() : BigDecimal.ZERO;
    }

    @JsonProperty("min")
    public BigDecimal getMin() {
        return getCount() > 0 ? summary.getMin() : BigDecimal.ZERO;
    }

    @JsonProperty("count")
    public long getCount() {
        return summary.getCount();
    }
}