package com.n26;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    private static String transactionJson(String amount, String timestamp) {
        return String.format("{\"amount\":\"%s3\",\"timestamp\":\"%s\"}", amount, timestamp);
    }

    private HttpStatus postTransactionStatus(String jsonBody) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> req = new HttpEntity<>(jsonBody, headers);
        return restTemplate.postForEntity("/transactions", req, Void.class).getStatusCode();
    }

    @Test
    public void testPostTransactionInvalidJSON() {
        assertThat(postTransactionStatus("BAZINGA!")).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testPostTransactionInvalidAmount() {
        assertThat(postTransactionStatus(transactionJson(
                "One hundred",
                Instant.now().toString()
        ))).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testPostTransactionInvalidTimestamp() {
        assertThat(postTransactionStatus(transactionJson(
                "12.3",
                "yesterday"
        ))).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testPostTransactionFutureTimestamp() {
        assertThat(postTransactionStatus(transactionJson(
                "12.3",
                Instant.now().plusSeconds(1).toString()
        ))).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testPostTransactionTooOld() {
        assertThat(postTransactionStatus(transactionJson(
                "12.3",
                Instant.now().minusMillis(60001).toString()
        ))).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testPostTransactionOk() {
        assertThat(postTransactionStatus(transactionJson(
                "12.3",
                Instant.now().toString()
        ))).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void testGetStatistics() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/statistics", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType())
                .matches(MediaType.APPLICATION_JSON::includes);
        assertThat(resp.getBody()).isNotNull();

        new JsonPathExpectationsHelper("$.count").assertValueIsNumber(resp.getBody());
        Stream.of("sum", "avg", "max", "min").forEach(fieldName ->
                new JsonPathExpectationsHelper("$.%s", fieldName).assertValueIsString(resp.getBody()));
    }

    @Test
    public void deleteTransactions() {
        ResponseEntity<String> resp = restTemplate.exchange("/transactions", HttpMethod.DELETE, null, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
