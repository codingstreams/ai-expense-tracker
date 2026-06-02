package com.example.et_core.dto;

public record AccountDto(
    String id,
    String bankName,
    String lastFour,
    String type,
    Double amount
) {}
