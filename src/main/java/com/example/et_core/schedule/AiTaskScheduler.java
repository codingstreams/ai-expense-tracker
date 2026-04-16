package com.example.et_core.schedule;

import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Status;
import com.example.et_core.service.ai.AiService;
import com.example.et_core.service.ai.parsetask.AiParseTaskService;
import com.example.et_core.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;

@Component
@RequiredArgsConstructor
public class AiTaskScheduler {
  private final AiParseTaskService aiParseTaskService;
  private final AiService aiService;

  private final PriorityQueue<AiParsingTask> taskQueue = new PriorityQueue<>(Comparator.comparing(AiParsingTask::getCreatedAt));

  @Scheduled(fixedRate = 5000)
  void scheduleAiTask() {
    if (taskQueue.isEmpty()) {
      final var tasks = aiParseTaskService.getPendingTasks(Status.PENDING);
      taskQueue.addAll(tasks);
    }

    if(!taskQueue.isEmpty()){
      final var aiParsingTask = taskQueue.remove();
      aiParsingTask.setStatus(Status.PROCESSING);
      aiParseTaskService.save(aiParsingTask);

      aiService.parse(aiParsingTask);
    }
  }
}
