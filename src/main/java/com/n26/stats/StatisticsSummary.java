package com.n26.stats;

public interface StatisticsSummary<T> {
    /**
     * Returns total sum of values.
     *
     * @return sum
     */
    T getSum();

    /**
     * Returns the single highest value.
     *
     * @return highest value
     */
    T getMax();

    /**
     * Returns the single lowest value.
     *
     * @return lowest value
     */
    T getMin();

    /**
     * Returns the total number of transactions.
     *
     * @return total number of transactions
     */
    long getCount();
}
