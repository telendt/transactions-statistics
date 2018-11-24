package com.n26;

interface StatisticsSummary<T> {
    T getSum();

    T getMax();

    T getMin();

    long getCount();
}
