package com.example.et_core.dto;

public record UpdateTransactionDto(
    Long transactionId,
    String type,
    String description,
    Double amount,
    String transactionDate,
    Long paymentModeId,
    Long accountId,
    Long categoryId,
    Long toAccountId
) {
}
