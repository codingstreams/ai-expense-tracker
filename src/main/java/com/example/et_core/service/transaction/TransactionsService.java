package com.example.et_core.service.transaction;

import com.example.et_core.dto.CreateTransactionDto;
import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.UpdateTransactionDto;

import java.util.List;

public interface TransactionsService {
    TransactionDto saveTransaction(String appUserId, CreateTransactionDto requestBody);

    List<TransactionDto> getAllTransactions(String appUserId);

    TransactionDto updateTransaction(String appUserId, UpdateTransactionDto requestBody);

    void deleteTransaction(String appUserId, Long transactionId);
}
