package com.example.et_core.exception;

public class PaymentModeNotFoundException extends RuntimeException{
    public PaymentModeNotFoundException(Long accountId) {
        super("Payment Mode not found with id " + accountId);
    }
}
