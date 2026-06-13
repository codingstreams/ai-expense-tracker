package com.example.et_core.service.transaction;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.InsufficientAccountBalanceException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.et_core.model.TransactionType;
import java.time.LocalDate;
import java.util.List;

public interface TransactionsService {
    TransactionDto saveTransaction(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException;

    Page<TransactionDto> getAllTransactions(
            String appUserId,
            LocalDate startDate,
            LocalDate endDate,
            Double minAmount,
            Double maxAmount,
            List<TransactionType> types,
            List<Long> categoryIds,
            List<Long> accountIds,
            List<Long> paymentModeIds,
            String search,
            Pageable pageable
    );

    TransactionDto updateTransaction(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException;

    void deleteTransaction(String appUserId, Long transactionId);

    List<TransactionDto> getRecentTransactions(String userId);
}
