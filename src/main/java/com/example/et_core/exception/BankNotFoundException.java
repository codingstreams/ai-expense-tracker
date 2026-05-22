package com.example.et_core.exception;

public class BankNotFoundException extends RuntimeException {
    public BankNotFoundException(Long bankId) {
        super("Bank not found with id " + bankId);
    }
}
