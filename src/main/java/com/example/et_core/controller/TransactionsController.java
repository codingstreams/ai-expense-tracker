package com.example.et_core.controller;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.service.transaction.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsController {
    private static final String LOGGED_IN_USER = "f6f2435f-08ac-4b8d-a705-8449ac607685";
    private final TransactionsService transactionsService;

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionRequestDto requestBody) throws InsufficientAccountBalanceException {
        final var responseBody = transactionsService.saveTransaction(LOGGED_IN_USER, requestBody);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        final var responseBody = transactionsService.getAllTransactions(LOGGED_IN_USER);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(responseBody);
    }

    @PatchMapping
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody TransactionRequestDto requestBody) throws InsufficientAccountBalanceException {
        final var responseBody = transactionsService.updateTransaction(LOGGED_IN_USER, requestBody);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(responseBody);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        transactionsService.deleteTransaction(LOGGED_IN_USER, transactionId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }
}
