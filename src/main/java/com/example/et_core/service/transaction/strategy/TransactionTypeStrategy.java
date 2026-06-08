package com.example.et_core.service.transaction.strategy;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.model.TransactionType;

public interface TransactionTypeStrategy {
  TransactionDto process(String appUserId, TransactionRequestDto dto, OperationType type) throws InsufficientAccountBalanceException;

  TransactionType getType();
}
