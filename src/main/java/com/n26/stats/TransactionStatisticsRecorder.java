package com.n26.stats;

import java.math.BigDecimal;
import java.time.Instant;

public interface TransactionStatisticsRecorder {
    /**
     * Records transaction of a given amount that happened at a given timestamp.
     *
     * @param amount    the value of transaction
     * @param timestamp the time of transaction
     * @return true if transaction happened in the last X seconds, false otherwise
     */
    boolean recordTransaction(BigDecimal amount, Instant timestamp);

    /**
     * Clears values of all recorded transactions.
     */
    void clear();

    /**
     * Returns a summary of all transactions that happened in the X seconds
     *
     * @return SummaryStatistics of recorded transactions
     */
    StatisticsSummary<BigDecimal> getSummary();
}
