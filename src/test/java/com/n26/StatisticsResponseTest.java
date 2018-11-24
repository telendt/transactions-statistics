package com.n26;

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
     *
     * @throws IOException
     */
    @Test
    public void testSerializationZeroValue() throws IOException {
        assertThat(json.write(StatisticsResponse.ZERO_VALUE)).isEqualToJson("expected/statisticsResponse-02.json");
    }

    /**
     * Test serialization of StatisticsResponse given in challenge instructions.
     *
     * @throws IOException
     */
    @Test
    public void testSerializationSpecExample() throws IOException {
        StatisticsResponse statisticsResponse = new StatisticsResponse(
                new BigDecimal("1000.00"),
                new BigDecimal("100.53"),
                new BigDecimal("200000.49"), // max greater that sum? how? :)
                new BigDecimal("50.23"),
                10);
        System.out.println(json.write(statisticsResponse).toString());
        assertThat(json.write(statisticsResponse)).isEqualToJson("expected/statisticsResponse-01.json");
    }
}
