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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.et_core.model.TransactionType;
import java.time.LocalDate;
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
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) List<TransactionType> types,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> accountIds,
            @RequestParam(required = false) List<Long> paymentModeIds,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        final var responseBody = transactionsService.getAllTransactions(
            userId, startDate, endDate, minAmount, maxAmount, types, categoryIds, accountIds, paymentModeIds, search, pageable
        );

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
