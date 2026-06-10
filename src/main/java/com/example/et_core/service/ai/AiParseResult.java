package com.example.et_core.service.ai;

import com.example.et_core.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AiParseResult(
    TransactionType type,
    String description,
    Double amount,
    String date,
    String errorMessage,
    String category
) {
    @JsonCreator
    public AiParseResult(
        @JsonProperty("type") TransactionType type,
        @JsonProperty("description") String description,
        @JsonProperty("amount") Double amount,
        @JsonProperty("date") String date,
        @JsonProperty("errorMessage") String errorMessage,
        @JsonProperty("category") String category
    ) {
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.errorMessage = errorMessage;
        this.category = category;
    }
}
