package com.example.et_core.dto;

public record UserConfigDto(
    String language,
    Long defaultPaymentModeId
) {}
