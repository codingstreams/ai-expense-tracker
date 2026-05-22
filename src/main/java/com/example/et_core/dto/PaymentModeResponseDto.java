package com.example.et_core.dto;

import com.example.et_core.service.transaction.TransactionBehavior;

public record PaymentModeResponseDto(
    Long id,
    String name,
    TransactionBehavior type
) {}
