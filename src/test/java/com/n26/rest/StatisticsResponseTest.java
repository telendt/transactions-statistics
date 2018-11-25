package com.n26.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.stats.StatisticsSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class StatisticsResponseTest {
    private static final FakeSummary ZERO_SUMMARY = new FakeSummary(null, null, null, 0);
    private static final FakeSummary SPEC_SUMMARY = new FakeSummary(
            new BigDecimal("1005.3"), // corrected to get the value of avg from the spec
            new BigDecimal("200000.49"), // max greater that sum? how? :)
            new BigDecimal("50.23"),
            10
    );

    @Autowired
    private ObjectMapper objectMapper;

    // IntelliJ doesn't like autowiring JacksonTester instance
    private JacksonTester<StatisticsResponse> json;

    @Before
    public void setup() {
        JacksonTester.initFields(this, objectMapper);
    }

    /**
     * Test serialization of empty (ZERO_VALUE) StatisticsResponse.
     * Such value is returned for GET /statistics when there were no transactions in the last 60s.
     */
    @Test
    public void testSerializationZeroValue() throws IOException {
        assertThat(json.write(new StatisticsResponse(ZERO_SUMMARY, 2, RoundingMode.HALF_UP)))
                .isEqualToJson("expected/statisticsResponse-01.json");
    }

    /**
     * Test serialization of StatisticsResponse given in challenge instructions.
     */
    @Test
    public void testSerializationSpecExample() throws IOException {
        assertThat(json.write(new StatisticsResponse(SPEC_SUMMARY, 2, RoundingMode.HALF_UP)))
                .isEqualToJson("expected/statisticsResponse-02.json");
    }

    /**
     * Test serialization of StatisticsResponse with different scale and rounding.
     */
    @Test
    public void testSerializationNonDefaultScaleAndRounding() throws IOException {
        assertThat(json.write(new StatisticsResponse(SPEC_SUMMARY, 1, RoundingMode.FLOOR)))
                .isEqualToJson("expected/statisticsResponse-03.json");
    }

    private static class FakeSummary implements StatisticsSummary<BigDecimal> {
        private final BigDecimal sum, max, min;
        private final long count;

        FakeSummary(BigDecimal sum, BigDecimal max, BigDecimal min, long count) {
            this.sum = sum;
            this.max = max;
            this.min = min;
            this.count = count;
        }

        @Override
        public BigDecimal getSum() {
            return sum;
        }

        @Override
        public BigDecimal getMax() {
            return max;
        }

        @Override
        public BigDecimal getMin() {
            return min;
        }

        @Override
        public long getCount() {
            return count;
        }
    }
}
