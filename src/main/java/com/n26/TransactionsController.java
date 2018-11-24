package com.n26;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@RestController("/")
public class TransactionsController {

    private final TransactionStatistics transactionStatistics;
    private final Clock clock;

    TransactionsController(TransactionStatistics transactionStatistics,
                           Clock clock) {
        this.transactionStatistics = transactionStatistics;
        this.clock = clock;
    }

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity postTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
        // check if transaction is not too old
        // (more precise than relying on transactionStatistics.recordTransaction return value)
        Instant timestamp = transactionRequest.getTimestamp();
        if (Duration.between(timestamp, Instant.now(clock)).compareTo(Duration.ofSeconds(60)) > 0) { // TODO: parametrize
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        transactionStatistics.recordTransaction(transactionRequest.getAmount(), timestamp);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    StatisticsResponse getStatistics() {
        return new StatisticsResponse(transactionStatistics.getSummary());
    }

    @DeleteMapping(value = "/transactions")
    ResponseEntity deleteTransactions() {
        transactionStatistics.clear();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler
    ResponseEntity handleThrowable(MethodArgumentNotValidException e) {
        return new ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    ResponseEntity handleThrowable(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException
                || cause instanceof InvalidNullException
                || cause instanceof InvalidTypeIdException
                || cause instanceof PropertyBindingException) {
            System.out.println(cause.toString());
            return new ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
