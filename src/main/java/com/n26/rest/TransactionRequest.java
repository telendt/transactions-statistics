package com.n26.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionRequest {
    private final BigDecimal amount;


    private final Instant timestamp;

    public TransactionRequest(@JsonProperty("amount") BigDecimal amount, @JsonProperty("timestamp") Instant timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

    @NotNull
    BigDecimal getAmount() {
        return amount;
    }

    @PastOrPresent
    @NotNull
    Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("TransactionRequest{format=%s; timestamp=%d}", amount, timestamp.toEpochMilli());
    }
}
