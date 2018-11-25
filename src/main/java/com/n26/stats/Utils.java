package com.n26.stats;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Utils {
    /**
     * Returns stream of objects from AtomicReferenceArray.
     *
     * @param arr AtomicReferenceArray instance
     * @param <T> The base class of elements held in arr
     * @return stream of objects
     */
    static <T> Stream<T> stream(AtomicReferenceArray<T> arr) {
        return IntStream.range(0, arr.length()).mapToObj(arr::get);
    }

    /**
     * Returns number of whole times a specified Duration occurs within this Duration.
     * <p>
     * This code was backported from JDK 9.
     *
     * @param duration the value of duration to divide
     * @param divisor  the value to divide the duration by, positive or negative, not null
     * @return number of whole times, rounded toward zero, a specified
     * {@code Duration} occurs within this Duration, may be negative
     * @throws ArithmeticException if the divisor is zero, or if numeric overflow occurs
     */
    static long durationDividedBy(Duration duration, Duration divisor) {
        BigDecimal v1 = BigDecimal.valueOf(duration.getSeconds()).add(BigDecimal.valueOf(duration.getNano(), 9));
        BigDecimal v2 = BigDecimal.valueOf(divisor.getSeconds()).add(BigDecimal.valueOf(divisor.getNano(), 9));
        return v1.divideToIntegralValue(v2).longValueExact();
    }
}
