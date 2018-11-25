package com.n26.stats;

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

/**
 * Thread safe implementation of <tt>TransactionStatisticsRecorder</tt> interface with constant time and space
 * implementation of specified methods.
 *
 * <p>It works by splitting time window into smaller "buckets", each of width <tt>Δt/C</tt>,
 * holding aggregates of transactions that happened in this small time frame.
 * These "buckets" remain fixed in time, they don't "slide". The effect of
 * "sliding" is simulated by periodically appending new buckets and removing <i>expired</i>
 * ones.
 *
 * <p>Whenever a new transaction is being registered we update aggregates of
 * specific bucket, that fulfills the following criteria:
 *
 * <pre>
 * index * Δt/C &lt;= T - t' &lt; (index + 1) * Δt/C
 * </pre>
 * <p>
 * Where:
 * <ul>
 * <li><tt>index</tt> - bucket index (starting from 0)</li>
 * <li><tt>C</tt> - number of buckets ("resolution")</li>
 * <li><tt>T</tt> - current timestamp <i>quantized</i></li>
 * <li><tt>t'</tt> - transaction timestamp</li>
 * <li><tt>Δt</tt> - time window width</li>
 * </ul>
 */
public class TransactionStatisticsRecorderImpl implements TransactionStatisticsRecorder {
    private final Logger logger = LoggerFactory.getLogger(TransactionStatisticsRecorderImpl.class);

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

    /**
     * Constructs TransactionStatisticsRecorderImpl of given time window (equals to maxTransactionAge)
     * and resolution (number of buckets).
     *
     * @param maxTransactionAge maximum age of a transaction
     * @param resolution        number of buckets (affects precision of summary statistics)
     * @param clock             custom {@code Clock} instance
     * @throws IllegalArgumentException
     */
    public TransactionStatisticsRecorderImpl(Duration maxTransactionAge, int resolution, Clock clock) {
        Objects.requireNonNull(maxTransactionAge, "maxTransactionAge");
        if (maxTransactionAge.isNegative() || maxTransactionAge.isZero()) {
            throw new IllegalArgumentException("Illegal maxTransactionAge: non-positive value");
        }
        if (resolution < 1) {
            throw new IllegalArgumentException("Illegal resolution: " + resolution);
        }

        buckets = new AtomicReferenceArray<>(resolution);
        this.clock = clock;
        tickDelta = maxTransactionAge.dividedBy(resolution);
        state = new State(clock.instant().plus(tickDelta), 0);
    }

    /**
     * Tick should be called periodically (with a fixed rate of duration/resolution) by an external scheduler.
     */
    public void tick() {
        try {
            writeLock.lock();
            int i = (state.readIndex > 0 ? state.readIndex : buckets.length()) - 1;
            buckets.set(i, null);
            state = new State(clock.instant().plus(tickDelta), i);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns bucket index for a given timestamp.
     *
     * @param timestamp the point in time
     * @return bucket index for a given timestamp, -1 if out of bounds
     */
    private int getIndex(Instant timestamp) {
        State s = state;
        long l;
        try {
            l = Utils.durationDividedBy(Duration.between(timestamp, s.timeZero), tickDelta);
        } catch (ArithmeticException e) {
            return -1;
        }
        if (l > buckets.length() || l < 0) {
            return -1;
        }
        return (s.readIndex + (int) l) % buckets.length();
    }

    /**
     * Records transaction of a given amount that happened at a given timestamp.
     * Runs in constant time O(1).
     *
     * @param amount    the value of transaction
     * @param timestamp the time of transaction
     * @return true if transaction happened between now and now-maxTransactionAge, false otherwise
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public boolean recordTransaction(BigDecimal amount, Instant timestamp) {
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
                        new Stats(amount, amount, amount, 1) :
                        new Stats(
                                prev.sum.add(amount),
                                prev.max.compareTo(amount) > 0 ? prev.max : amount,
                                prev.min.compareTo(amount) < 0 ? prev.min : amount,
                                prev.count + 1
                        );
            } while (!buckets.compareAndSet(i, prev, next));
            return true;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Clears values of all recorded transactions.
     * Runs in constant time O(1) (linear to the number of buckets but constant to the number of recorded transactions).
     */
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

    /**
     * Returns a summary of all transactions that happened between now and now-maxTransactionAge.
     * Runs in constant time O(1) (linear to the number of buckets but constant to the number of recorded transactions).
     *
     * @return SummaryStatistics of recorded transactions
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public StatisticsSummary<BigDecimal> getSummary() {
        Stats finalStats;
        try {
            readLock.lock();
            finalStats = Utils.stream(buckets)
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
