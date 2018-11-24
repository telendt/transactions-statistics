package com.n26;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class TransactionStatisticsImpl implements TransactionStatistics {
    private final Logger logger = LoggerFactory.getLogger(TransactionStatisticsImpl.class);

    private final AtomicReferenceArray<Stats> buckets;
    private final Clock clock;
    private final Duration tickDelta;
    // locking between tick/clear (single) and recordTransaction/getSummary (many)
    private final Lock readLock;
    private final Lock writeLock;
    private volatile State state; // atomic reference

    {
        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    TransactionStatisticsImpl(Duration duration, int length, Clock clock) {
        buckets = new AtomicReferenceArray<>(length);
        this.clock = clock;
        tickDelta = duration.dividedBy(length);
        state = new State(Instant.now(clock).plus(tickDelta), 0);
    }

    private static <T> Stream<T> stream(AtomicReferenceArray<T> arr) {
        return IntStream.range(0, arr.length()).mapToObj(arr::get);
    }

    void tick() {
        try {
            writeLock.lock();
            int i = (state.readIndex > 0 ? state.readIndex : buckets.length()) - 1;
            buckets.set(i, null);
            state = new State(Instant.now(clock).plus(tickDelta), i);
        } finally {
            writeLock.unlock();
        }
    }

    private int getIndex(Instant timestamp) {
        State s = state;
        // duration1.dividedBy(duration2) is available only in Java >= 9.
        long l = Duration.between(timestamp, s.timeZero).toNanos() / tickDelta.toNanos();
        if (l > buckets.length()) {
            return -1;
        }
        return (s.readIndex + (int) l) % buckets.length();
    }

    @Override
    public boolean recordTransaction(BigDecimal value, Instant timestamp) {
        try {
            readLock.lock();
            Stats prev, next;
            int i;
            do { // optimistic update loop
                i = getIndex(timestamp);
                if (i < 0) {
                    return false;
                }
                prev = buckets.get(i);
                next = prev == null ?
                        new Stats(value, value, value, 1) :
                        new Stats(
                                prev.sum.add(value),
                                prev.max.compareTo(value) > 0 ? prev.max : value,
                                prev.min.compareTo(value) < 0 ? prev.min : value,
                                prev.count + 1
                        );
            } while (!buckets.compareAndSet(i, prev, next));
            return true;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            writeLock.lock();
            for (int i = 0; i < buckets.length(); i++) {
                buckets.set(i, null);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public StatisticsSummary<BigDecimal> getSummary() {
        Stats finalStats;
        try {
            readLock.lock();
            finalStats = stream(buckets)
                    .filter(Objects::nonNull)
                    .reduce((prev, next) -> new Stats(
                            prev.sum.add(next.sum),
                            prev.max.compareTo(next.max) > 0 ? prev.max : next.max,
                            prev.min.compareTo(next.min) < 0 ? prev.min : next.min,
                            prev.count + next.count
                    )).orElse(Stats.ZERO_VALUE);
        } finally {
            readLock.unlock();
        }

        return new StatisticsSummary<BigDecimal>() {
            @Override
            public BigDecimal getSum() {
                return finalStats.sum;
            }

            @Override
            public BigDecimal getMax() {
                return finalStats.max;
            }

            @Override
            public BigDecimal getMin() {
                return finalStats.min;
            }

            @Override
            public long getCount() {
                return finalStats.count;
            }
        };
    }

    private static class State {
        final Instant timeZero;
        final int readIndex;

        State(Instant timeZero, int readIndex) {
            this.timeZero = timeZero;
            this.readIndex = readIndex;
        }
    }

    private static class Stats {
        static final Stats ZERO_VALUE = new Stats(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

        final BigDecimal sum;
        final BigDecimal max;
        final BigDecimal min;
        final long count;

        Stats(BigDecimal sum, BigDecimal max, BigDecimal min, long count) {
            this.sum = sum;
            this.max = max;
            this.min = min;
            this.count = count;
        }
    }
}
