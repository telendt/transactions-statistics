package com.n26.rest;

import com.n26.stats.StatisticsSummary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class StatisticsResponseTest {
    @Autowired
    private JacksonTester<StatisticsResponse> json;

    /**
     * Test serialization of empty (ZERO_VALUE) StatisticsResponse.
     * Such value is returned for GET /statistics when there were no transactions in the last 60s.
     */
    @Test
    public void testSerializationZeroValue() throws IOException {
        assertThat(json.write(new StatisticsResponse(new StatisticsSummary<BigDecimal>() {
            @Override
            public BigDecimal getSum() {
                return null;
            }

            @Override
            public BigDecimal getMax() {
                return null;
            }

            @Override
            public BigDecimal getMin() {
                return null;
            }

            @Override
            public long getCount() {
                return 0;
            }
        }))).isEqualToJson("expected/statisticsResponse-01.json");
    }

    /**
     * Test serialization of StatisticsResponse given in challenge instructions.
     */
    @Test
    public void testSerializationSpecExample() throws IOException {
        StatisticsResponse statisticsResponse = new StatisticsResponse(
                new StatisticsSummary<BigDecimal>() {
                    @Override
                    public BigDecimal getSum() {
                        return new BigDecimal("1005.3"); // corrected to get the value of avg from the spec
                    }

                    @Override
                    public BigDecimal getMax() {
                        return new BigDecimal("200000.49"); // max greater that sum? how? :)
                    }

                    @Override
                    public BigDecimal getMin() {
                        return new BigDecimal("50.23");
                    }

                    @Override
                    public long getCount() {
                        return 10;
                    }
                }
        );
        assertThat(json.write(statisticsResponse)).isEqualToJson("expected/statisticsResponse-02.json");
    }
}
