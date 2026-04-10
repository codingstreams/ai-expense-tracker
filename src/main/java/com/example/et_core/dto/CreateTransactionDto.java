package com.example.et_core.dto;

public record CreateTransactionDto (
        String type,
        String description,
        Double amount,
        String transactionDate,
        Long paymentModeId,
        Long accountId,
        Long categoryId
){
}
