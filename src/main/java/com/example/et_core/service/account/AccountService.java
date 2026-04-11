package com.example.et_core.service.account;

import com.example.et_core.exception.InsufficientAccountBalanceException;
import com.example.et_core.model.Account;

import java.util.List;

public interface AccountService {
    boolean existsByUserAndAccount(String appUserId, List<Long> accounts);

  void updateBalance(Long accountId, Double amount, Long paymentModeId, String type, boolean isSourceAccount) throws InsufficientAccountBalanceException;

  Account get(Long accountId);

  void update(Account account);
}
