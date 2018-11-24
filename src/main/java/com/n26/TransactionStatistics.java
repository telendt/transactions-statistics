package com.n26;

import java.math.BigDecimal;
import java.time.Instant;

interface TransactionStatistics {
    boolean recordTransaction(BigDecimal value, Instant timestamp);

    void clear();

    StatisticsSummary<BigDecimal> getSummary();
}
