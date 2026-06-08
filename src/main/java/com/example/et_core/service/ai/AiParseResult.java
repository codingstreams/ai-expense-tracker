package com.example.et_core.service.ai;

import com.example.et_core.model.TransactionType;

public record AiParseResult(
    TransactionType type,
    String description,
    Double amount,
    String date,
    String errorMessage,
    String category
) {
}
