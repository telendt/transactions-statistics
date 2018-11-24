package com.n26;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionRequest {
    @NotNull
    private final BigDecimal amount;

    @PastOrPresent
    @NotNull
    private final Instant timestamp;

    public TransactionRequest(BigDecimal amount, Instant timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

    BigDecimal getAmount() {
        return amount;
    }

    Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("TransactionRequest{format=%s; timestamp=%d}", amount, timestamp.toEpochMilli());
    }
}
