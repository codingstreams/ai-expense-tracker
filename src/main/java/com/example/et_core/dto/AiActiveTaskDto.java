package com.example.et_core.dto;

import com.example.et_core.model.Status;

public record AiActiveTaskDto(
    String jobId,
    Status status
) {
}
