package com.example.et_core.dto;

public record AiInsightResponseDto(
    Long id,
    String type,
    String insightText,
    Long createdAt,
    String status
) {
}
