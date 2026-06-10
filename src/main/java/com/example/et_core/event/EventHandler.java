package com.example.et_core.event;

import com.example.et_core.mapper.TransactionMapper;
import com.example.et_core.service.ai.AiParseResult;
import com.example.et_core.service.ai.parsetask.AiParseTaskService;
import com.example.et_core.service.category.CategoryService;
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
  private final TransactionsService transactionsService;
  private final UserConfigService userConfigService;
  private final AiParseTaskService aiParseTaskService;
  private final CategoryService categoryService;
  private final ObjectMapper mapper;

  /**
   * Handles the completed AI parsing task by saving the result as a transaction
   * and notifying the client only AFTER the save succeeds.
   * This eliminates the race condition where the client was notified before the
   * DB write.
   */
  @Async
  @EventListener(AiParsingTaskCompleted.class)
  public void saveResultAsTxn(AiParsingTaskCompleted event) {
    log.info("Ai parsing task completed. Converting and saving data into DB. Job ID: {}", event.jobId());
    final var jobId = event.jobId().toString();

    try {
      final var task = aiParseTaskService.getByIdWithAppUser(event.jobId());
      final var appUserId = task.getAppUser().getId();
      log.info("Processing transaction parsing result for user ID: {}", appUserId);

      // Get default Payment mode and account
      final var userConfig = userConfigService.getByUserId(appUserId);

      final var aiParseResult = mapper.readValue(event.task().getContent(), AiParseResult.class);
      log.info("Parsed AI result content: {}", aiParseResult);

      if (aiParseResult.errorMessage() != null && !aiParseResult.errorMessage().isEmpty()) {
        log.warn("AI parsing returned error: {}. Aborting transaction save.", aiParseResult.errorMessage());
        return;
      }

      final var category = categoryService.getSystemCategoryByName(aiParseResult.category());

      final var requestDto = TransactionMapper.INSTANCE.fromAiParseTask(
          aiParseResult, // Task --> Source
          userConfig.getDefaultPaymentMode().getId(), // Payment Mode Id
          userConfig.getDefaultAccount().getId(), // Account ID
          category.getId() // System Category Id
      );

      log.info("Saving transaction: {}", requestDto);
      transactionsService.saveTransaction(appUserId, requestDto);
      log.info("Transaction saved successfully for Job ID: {}", event.jobId());

      // Notify client AFTER successful save to prevent false-positive "Success"

    } catch (Exception e) {
      log.error("Failed to save transaction for completed AI parsing task. Job ID: {}", event.jobId(), e);
      try {
      } catch (Exception notifyEx) {
        log.error("Failed to send FAILED notification status to client for Job ID: {}", event.jobId(), notifyEx);
      }
    }
  }

  @EventListener(AiParsingTaskCreated.class)
  public void openConnection(AiParsingTaskCreated event) {
    log.info("Ai parsing task created. Opening connection...");
  }
}
