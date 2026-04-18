package com.example.et_core.service.ai;

import com.example.et_core.model.TransactionType;

public record AiParseResult(
    TransactionType type,
    String description,
    Double amount,
    String transactionDate,
    String errorMessage
) {
}
