package com.example.et_core.dto;

public record TransactionDto(
        String type,
        String description,
        Double amount,
       String transactionDate
){
}
