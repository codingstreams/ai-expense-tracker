package com.example.et_core.dto;

public record TransactionDto(
    String transactionId,
    String type,
    String description,
    Double amount,
    String transactionDate,
    String transferId,
    CategoryDto category
){
}
