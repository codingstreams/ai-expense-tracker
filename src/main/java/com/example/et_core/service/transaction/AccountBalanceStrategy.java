package com.example.et_core.service.transaction;

import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.model.Account;
import com.example.et_core.model.TransactionType;

public interface AccountBalanceStrategy {
  Double calculateBalance(Account account, Double amount, TransactionType transactionType, boolean isSourceAccount) throws InsufficientAccountBalanceException;

  Double reverseBalance(Account account, Double previousAmount, TransactionType transactionType, boolean isSourceAccount);

  void validate(Account account, Double amount) throws InsufficientAccountBalanceException;

  TransactionBehavior getType();
}
