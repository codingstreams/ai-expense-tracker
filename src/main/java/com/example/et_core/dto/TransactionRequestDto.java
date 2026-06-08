package com.example.et_core.dto;

public record TransactionRequestDto(
    Long transactionId,
    String type,
    String description,
    Double amount,
    String transactionDate,
    Long paymentModeId,
    Long accountId,
    Long categoryId,
    Long toAccountId,
    String transferId
) {
}
