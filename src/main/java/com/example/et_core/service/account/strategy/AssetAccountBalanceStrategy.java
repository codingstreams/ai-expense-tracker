package com.example.et_core.service.account.strategy;

import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.model.Account;
import com.example.et_core.model.TransactionType;
import com.example.et_core.service.transaction.TransactionBehavior;
import org.springframework.stereotype.Component;

@Component("AssetAccountBalanceStrategy")
public class AssetAccountBalanceStrategy implements AccountBalanceStrategy {
  @Override
  public Double calculateBalance(Account account, Double amount, TransactionType transactionType, boolean isSourceAccount) throws InsufficientAccountBalanceException {
    if (transactionType == TransactionType.TRANSFER) {
      if (isSourceAccount) {
        validate(account, amount);
        return account.getBalance() - amount;
      }

      return account.getBalance() + amount;
    }

    if(transactionType == TransactionType.EXPENSE)
      validate(account, amount);

    return transactionType == TransactionType.EXPENSE ?
        account.getBalance() - amount :
        account.getBalance() + amount;
  }

  @Override
  public Double reverseBalance(Account account, Double previousAmount, TransactionType transactionType, boolean isSourceAccount) {
    if (transactionType == TransactionType.TRANSFER) {
      if (isSourceAccount) {
        return account.getBalance() + previousAmount;
      }

      return account.getBalance() - previousAmount;
    }

    return transactionType == TransactionType.EXPENSE ?
        account.getBalance() + previousAmount :
        account.getBalance() - previousAmount;
  }

  @Override
  public void validate(Account account, Double amount) throws InsufficientAccountBalanceException {
    final var currentBalance = account.getBalance();
    if (amount > currentBalance) {
      throw new InsufficientAccountBalanceException(account.getId());
    }
  }

  @Override
  public TransactionBehavior getType() {
    return TransactionBehavior.ASSET;
  }
}
