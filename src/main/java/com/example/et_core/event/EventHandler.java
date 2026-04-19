package com.example.et_core.event;

import com.example.et_core.dto.JobStatusDto;
import com.example.et_core.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {
  private final NotificationService notificationService;

  @Async
  @EventListener(AiParsingTaskCompleted.class)
  public void notifyClient(AiParsingTaskCompleted event) {
    log.info("Ai parsing task completed. Notifying client...");
    final var jobStatus = JobStatusDto.of(
        event.jobId().toString(),
        event.task().getStatus().name());

    notificationService.send(jobStatus);
    notificationService.closeConnection(event.jobId().toString());
  }

  @EventListener(AiParsingTaskCreated.class)
  public void openConnection(AiParsingTaskCreated event) {
    log.info("Ai parsing task created. Opening connection...");
    notificationService.openConnection(event.jobId().toString());
  }
}
