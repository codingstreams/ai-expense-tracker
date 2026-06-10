package com.example.et_core.event;

import com.example.et_core.dto.JobStatusDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.model.*;
import com.example.et_core.service.ai.AiParseResult;
import com.example.et_core.service.ai.parsetask.AiParseTaskService;
import com.example.et_core.service.category.CategoryService;
import com.example.et_core.service.notification.NotificationService;
import com.example.et_core.service.transaction.TransactionsService;
import com.example.et_core.service.userconfig.UserConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventHandlerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private TransactionsService transactionsService;

  @Mock
  private UserConfigService userConfigService;

  @Mock
  private AiParseTaskService aiParseTaskService;

  @Mock
  private CategoryService categoryService;

  @Mock
  private ObjectMapper mapper;

  private EventHandler eventHandler;

  @BeforeEach
  void setUp() {
    eventHandler = new EventHandler(
        notificationService,
        transactionsService,
        userConfigService,
        aiParseTaskService,
        categoryService,
        mapper
    );
  }

  @Test
  void shouldSaveTransactionSuccessfully_WhenAiParsingTaskSucceeds() throws Exception {
    // Arrange
    final Long jobId = 1L;
    final String appUserId = "user-123";

    final AppUser appUser = AppUser.builder().id(appUserId).build();
    final AiParsingTask task = AiParsingTask.builder()
        .id(jobId)
        .appUser(appUser)
        .content("{\"description\": \"Coffee\", \"amount\": 4.5}")
        .status(Status.COMPLETED)
        .build();

    final AiParsingTaskCompleted event = new AiParsingTaskCompleted(jobId, task);

    final PaymentMode paymentMode = PaymentMode.builder().id(10L).build();
    final Account account = Account.builder().id(20L).build();
    final UserConfig userConfig = UserConfig.builder()
        .defaultPaymentMode(paymentMode)
        .defaultAccount(account)
        .build();

    final AiParseResult aiParseResult = new AiParseResult(
        TransactionType.EXPENSE,
        "Coffee",
        4.5,
        "10-06-2026",
        null,
        "Food"
    );

    final SystemCategory systemCategory = SystemCategory.builder().id(30L).name("Food").build();

    when(aiParseTaskService.getByIdWithAppUser(jobId)).thenReturn(task);
    when(userConfigService.getByUserId(appUserId)).thenReturn(userConfig);
    when(mapper.readValue(eq("{\"description\": \"Coffee\", \"amount\": 4.5}"), eq(AiParseResult.class)))
        .thenReturn(aiParseResult);
    when(categoryService.getSystemCategoryByName("Food")).thenReturn(systemCategory);

    // Act
    eventHandler.saveResultAsTxn(event);

    // Assert
    final ArgumentCaptor<TransactionRequestDto> requestCaptor = ArgumentCaptor.forClass(TransactionRequestDto.class);
    verify(transactionsService).saveTransaction(eq(appUserId), requestCaptor.capture());

    final TransactionRequestDto requestDto = requestCaptor.getValue();
    assertEquals("Coffee", requestDto.description());
    assertEquals(4.5, requestDto.amount());
    assertEquals("EXPENSE", requestDto.type());
    assertEquals("10-06-2026", requestDto.transactionDate());
    assertEquals(10L, requestDto.paymentModeId());
    assertEquals(20L, requestDto.accountId());
    assertEquals(30L, requestDto.categoryId());

    verify(notificationService).send(argThat(dto -> dto != null && "1".equals(dto.jobId()) && "COMPLETED".equals(dto.status())));
    verify(notificationService).closeConnection("1");
  }

  @Test
  void shouldNotSaveTransaction_WhenAiParsingTaskContainsError() throws Exception {
    // Arrange
    final Long jobId = 1L;
    final String appUserId = "user-123";

    final AppUser appUser = AppUser.builder().id(appUserId).build();
    final AiParsingTask task = AiParsingTask.builder()
        .id(jobId)
        .appUser(appUser)
        .content("{\"errorMessage\": \"Unable to parse receipt\"}")
        .status(Status.FAILED)
        .build();

    final AiParsingTaskCompleted event = new AiParsingTaskCompleted(jobId, task);

    final PaymentMode paymentMode = PaymentMode.builder().id(10L).build();
    final Account account = Account.builder().id(20L).build();
    final UserConfig userConfig = UserConfig.builder()
        .defaultPaymentMode(paymentMode)
        .defaultAccount(account)
        .build();

    final AiParseResult aiParseResult = new AiParseResult(
        null,
        null,
        null,
        null,
        "Unable to parse receipt",
        null
    );

    when(aiParseTaskService.getByIdWithAppUser(jobId)).thenReturn(task);
    when(userConfigService.getByUserId(appUserId)).thenReturn(userConfig);
    when(mapper.readValue(eq("{\"errorMessage\": \"Unable to parse receipt\"}"), eq(AiParseResult.class)))
        .thenReturn(aiParseResult);

    // Act
    eventHandler.saveResultAsTxn(event);

    // Assert
    verify(transactionsService, never()).saveTransaction(any(), any());
    verify(categoryService, never()).getSystemCategoryByName(any());
    verify(notificationService).send(argThat(dto -> dto != null && "1".equals(dto.jobId()) && "FAILED".equals(dto.status())));
    verify(notificationService).closeConnection("1");
  }

  @Test
  void shouldSendFailedNotification_WhenExceptionIsThrownDuringProcessing() throws Exception {
    // Arrange
    final Long jobId = 1L;
    final AiParsingTaskCompleted event = new AiParsingTaskCompleted(jobId, null);

    when(aiParseTaskService.getByIdWithAppUser(jobId)).thenThrow(new RuntimeException("Database error"));

    // Act
    eventHandler.saveResultAsTxn(event);

    // Assert
    verify(transactionsService, never()).saveTransaction(any(), any());
    verify(notificationService).send(argThat(dto -> dto != null && "1".equals(dto.jobId()) && "FAILED".equals(dto.status())));
    verify(notificationService).closeConnection("1");
  }

  @Test
  void shouldLogAndNotPropagateException_WhenNotificationFailsDuringExceptionHandling() throws Exception {
    // Arrange
    final Long jobId = 1L;
    final AiParsingTaskCompleted event = new AiParsingTaskCompleted(jobId, null);

    when(aiParseTaskService.getByIdWithAppUser(jobId)).thenThrow(new RuntimeException("Database error"));
    doThrow(new RuntimeException("Notification service offline"))
        .when(notificationService).send(any(JobStatusDto.class));

    // Act & Assert
    assertDoesNotThrow(() -> eventHandler.saveResultAsTxn(event));
    verify(notificationService).send(argThat(dto -> dto != null && "1".equals(dto.jobId()) && "FAILED".equals(dto.status())));
  }

  @Test
  void shouldOpenConnection_WhenAiParsingTaskCreatedEventReceived() {
    // Arrange
    final Long jobId = 1L;
    final AiParsingTaskCreated event = new AiParsingTaskCreated(jobId);

    // Act
    eventHandler.openConnection(event);

    // Assert
    verify(notificationService).openConnection("1");
  }
}
