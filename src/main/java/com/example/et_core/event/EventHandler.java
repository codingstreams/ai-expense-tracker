package com.example.et_core.event;

import com.example.et_core.dto.JobStatusDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.mapper.TransactionMapper;
import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Category;
import com.example.et_core.model.UserConfig;
import com.example.et_core.service.ai.AiParseResult;
import com.example.et_core.service.ai.parsetask.AiParseTaskService;
import com.example.et_core.service.category.CategoryService;
import com.example.et_core.service.notification.NotificationService;
import com.example.et_core.service.transaction.TransactionsService;
import com.example.et_core.service.userconfig.UserConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {
  private final NotificationService notificationService;
  private final TransactionsService transactionsService;
  private final UserConfigService userConfigService;
  private final AiParseTaskService aiParseTaskService;
  private final CategoryService categoryService;
  private final ObjectMapper mapper;

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

  @Async
  @EventListener(AiParsingTaskCompleted.class)
  public void saveResultAsTxn(AiParsingTaskCompleted event) {
    log.info("Ai parsing task completed. Converting and saving data into DB.");

    final var task = aiParseTaskService.getByIdWithAppUser(event.jobId());

    final var appUserId = task.getAppUser().getId();

    // Get default Payment mode and account
    final var userConfig = userConfigService.getByUserId(appUserId);

    final var aiParseResult = mapper.readValue(event.task().getContent(), AiParseResult.class);

    final var category = categoryService.getByName(aiParseResult.category());

    final var requestDto = TransactionMapper.INSTANCE.fromAiParseTask(
        aiParseResult, // Task --> Source
        userConfig.getDefaultPaymentMode().getId(), // Payment Mode Id
        userConfig.getDefaultAccount().getId(), // Account ID
        category.getId()  // Category Id
    );

    transactionsService.saveTransaction(appUserId, requestDto);
  }

  @EventListener(AiParsingTaskCreated.class)
  public void openConnection(AiParsingTaskCreated event) {
    log.info("Ai parsing task created. Opening connection...");
    notificationService.openConnection(event.jobId().toString());
  }
}
