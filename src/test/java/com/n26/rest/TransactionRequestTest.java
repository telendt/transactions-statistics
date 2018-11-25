package com.n26.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class TransactionRequestTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDeserializationSpecExample() throws IOException {
        String payload = "{\"amount\":\"12.3343\",\"timestamp\":\"2018-07-17T09:59:51.312Z\"}";
        TransactionRequest transactionRequest = objectMapper.readValue(payload, TransactionRequest.class);
        assertThat(transactionRequest.getAmount()).isEqualTo(new BigDecimal("12.3343"));
        assertThat(transactionRequest.getTimestamp()).isEqualTo(
                OffsetDateTime.of(2018, 7, 17, 9, 59, 51, 312000000, ZoneOffset.UTC).toInstant());
    }
}