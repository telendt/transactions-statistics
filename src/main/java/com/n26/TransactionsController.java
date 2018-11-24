package com.n26;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController("/")
public class TransactionsController {
    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity postTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    StatisticsResponse getStatistics() {
        return StatisticsResponse.ZERO_VALUE;
    }

    @DeleteMapping(value = "/transactions")
    ResponseEntity deleteTransactions() {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
