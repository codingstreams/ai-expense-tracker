package com.example.et_core.event;

import com.example.et_core.model.AiParsingTask;

public record AiParsingTaskCompleted(
    Long jobId,
    AiParsingTask task
) {
}
