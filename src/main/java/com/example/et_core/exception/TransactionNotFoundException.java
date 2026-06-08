package com.example.et_core.exception;

public class TransactionNotFoundException extends RuntimeException{
    public TransactionNotFoundException(Long accountId) {
        super("Transaction not found with id " + accountId);
    }
}
