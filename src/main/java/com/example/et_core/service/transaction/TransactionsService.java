package com.example.et_core.service.transaction;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.InsufficientAccountBalanceException;

import java.util.List;

public interface TransactionsService {
    TransactionDto saveTransaction(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException;

    List<TransactionDto> getAllTransactions(String appUserId);

    TransactionDto updateTransaction(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException;

    void deleteTransaction(String appUserId, Long transactionId);

    List<TransactionDto> getRecentTransactions(String userId);
}
