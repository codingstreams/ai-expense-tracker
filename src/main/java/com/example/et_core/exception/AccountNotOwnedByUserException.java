package com.example.et_core.exception;

import java.util.List;

public class AccountNotOwnedByUserException extends RuntimeException {
  public AccountNotOwnedByUserException(List<Long> accountIds, String userId) {
    super("Account IDs %s not owned by User %s".formatted(accountIds, userId));
  }
}
