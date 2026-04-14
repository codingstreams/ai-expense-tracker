package com.example.et_core.service.ai;

import com.example.et_core.model.TransactionType;

public record AiParseResult(
    TransactionType transactionType,
    String description,
    Double amount,
    String transactionDate,
    String errorMessage
) {
}
