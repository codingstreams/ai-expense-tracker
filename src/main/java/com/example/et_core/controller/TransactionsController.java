package com.example.et_core.controller;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.service.transaction.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsController {
        private final TransactionsService transactionsService;

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionRequestDto requestBody,@AuthenticationPrincipal String userId) throws InsufficientAccountBalanceException {
        final var responseBody = transactionsService.saveTransaction(userId, requestBody);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions(@AuthenticationPrincipal String userId) {
        final var responseBody = transactionsService.getAllTransactions(userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(responseBody);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TransactionDto>> getRecentTransactions(@AuthenticationPrincipal String userId) {
        final var responseBody = transactionsService.getRecentTransactions(userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(responseBody);
    }

    @PatchMapping
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody TransactionRequestDto requestBody, @AuthenticationPrincipal String userId) throws InsufficientAccountBalanceException {
        final var responseBody = transactionsService.updateTransaction(userId, requestBody);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(responseBody);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId, @AuthenticationPrincipal String userId) {
        transactionsService.deleteTransaction(userId, transactionId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }
}
