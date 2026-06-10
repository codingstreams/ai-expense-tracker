package com.example.et_core.schedule;

import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Status;
import com.example.et_core.service.ai.AiService;
import com.example.et_core.service.ai.parsetask.AiParseTaskService;
import com.example.et_core.service.notifications.NotificationService;
import com.example.et_core.service.notifications.NotificationService.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

@Component
@RequiredArgsConstructor
public class AiTaskScheduler {
  private final AiParseTaskService aiParseTaskService;
  private final AiService aiService;
  private final NotificationService notificationService;

  private final PriorityQueue<AiParsingTask> taskQueue = new PriorityQueue<>(
      Comparator.comparing(AiParsingTask::getCreatedAt));

  @Scheduled(fixedRate = 5000)
  void scheduleAiTask() {
    if (taskQueue.isEmpty()) {
      final var tasks = aiParseTaskService.getPendingTasksWithAppUser(Status.PENDING);
      taskQueue.addAll(tasks);
    }

    if (!taskQueue.isEmpty()) {
      final var aiParsingTask = taskQueue.remove();
      aiParsingTask.setStatus(Status.PROCESSING);
      aiParseTaskService.save(aiParsingTask);

      if (aiParsingTask.getAppUser() != null) {
        notificationService.send(
            aiParsingTask.getAppUser().getId(),
            null,
            NotificationEvent.AI_TASK_PROCESSING,
            Map.of("jobId", aiParsingTask.getId().toString(), "status", "PROCESSING")
        );
      }

      aiService.parse(aiParsingTask);
    }
  }
}
