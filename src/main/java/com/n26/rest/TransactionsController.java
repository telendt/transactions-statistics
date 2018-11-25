package com.n26.rest;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.n26.config.SerializationProperties;
import com.n26.stats.TransactionStatisticsRecorder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController("/")
public class TransactionsController {

    private final TransactionStatisticsRecorder transactionStatisticsRecorder;
    private final SerializationProperties serializationProperties;

    TransactionsController(TransactionStatisticsRecorder transactionStatisticsRecorder,
                           SerializationProperties serializationProperties) {
        this.transactionStatisticsRecorder = transactionStatisticsRecorder;
        this.serializationProperties = serializationProperties;
    }

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity postTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
        if (transactionStatisticsRecorder.recordTransaction(
                transactionRequest.getAmount(), transactionRequest.getTimestamp())) {
            return new ResponseEntity(HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    StatisticsResponse getStatistics() {
        return new StatisticsResponse(
                transactionStatisticsRecorder.getSummary(),
                serializationProperties.getDecimalPoints(),
                serializationProperties.getRoundingMode());
    }

    @DeleteMapping(value = "/transactions")
    ResponseEntity deleteTransactions() {
        transactionStatisticsRecorder.clear();
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
            return new ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
