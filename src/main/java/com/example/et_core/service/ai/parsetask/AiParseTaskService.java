package com.example.et_core.service.ai.parsetask;

import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Status;

import java.util.List;

public interface AiParseTaskService {
  AiParsingTask save(AiParsingTask aiParsingTask);

  List<AiParsingTask> getPendingTasks(Status status);

  List<AiParsingTask> getPendingTasksWithAppUser(Status status);

  AiParsingTask getByIdWithAppUser(Long aLong);

  List<AiParsingTask> getActiveTasks(String appUserId);
}
