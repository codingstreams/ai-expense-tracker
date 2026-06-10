package com.example.et_core.service.ai.analytics;

import com.example.et_core.model.AiInsightTask;
import com.example.et_core.model.AppUser;
import com.example.et_core.model.InsightType;
import com.example.et_core.model.Status;
import com.example.et_core.model.Transaction;
import com.example.et_core.repo.AiInsightTaskRepo;
import com.example.et_core.repo.AppUserRepo;
import com.example.et_core.repo.TransactionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAnalyticsServiceImplTest {

  @Mock
  private AppUserRepo appUserRepo;

  @Mock
  private AiInsightTaskRepo aiInsightTaskRepo;

  @Mock
  private TransactionRepo transactionRepo;

  @Mock
  private ChatClient chatClient;

  @Mock
  private ChatClient.ChatClientRequestSpec requestSpec;

  @Mock
  private ChatClient.CallResponseSpec responseSpec;

  private AtomicInteger requestCounter;

  private AiAnalyticsServiceImpl aiAnalyticsService;

  @BeforeEach
  void setUp() {
    requestCounter = new AtomicInteger(0);
    aiAnalyticsService = new AiAnalyticsServiceImpl(
        appUserRepo,
        aiInsightTaskRepo,
        transactionRepo,
        chatClient,
        requestCounter
    );
  }

  @Test
  void shouldGenerateInsightsForEligibleUsers_Success() {
    // Arrange
    AppUser user = AppUser.builder()
        .id("user-123")
        .email("user@example.com")
        .build();

    Transaction txn = Transaction.builder()
        .amount(100.0)
        .type(com.example.et_core.model.TransactionType.EXPENSE)
        .transactionDate(LocalDate.now().minusDays(2))
        .description("Groceries")
        .build();

    when(appUserRepo.findUsersEligibleForInsights(anyLong(), eq(PageRequest.of(0, 13))))
        .thenReturn(List.of(user));

    AiInsightTask processingTask = AiInsightTask.builder()
        .id(1L)
        .appUser(user)
        .type(InsightType.WEEKLY)
        .status(Status.PROCESSING)
        .build();

    when(aiInsightTaskRepo.save(any(AiInsightTask.class)))
        .thenAnswer(invocation -> {
          AiInsightTask argument = invocation.getArgument(0);
          return AiInsightTask.builder()
              .id(argument.getId() != null ? argument.getId() : 1L)
              .appUser(argument.getAppUser())
              .type(argument.getType())
              .status(argument.getStatus())
              .insightText(argument.getInsightText())
              .build();
        });

    when(transactionRepo.findRecentTransactions(eq("user-123"), any(LocalDate.class)))
        .thenReturn(List.of(txn));

    when(chatClient.prompt(anyString())).thenReturn(requestSpec);
    when(requestSpec.system(anyString())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenReturn("Top expense is Groceries.");

    // Act
    aiAnalyticsService.generateInsightsForEligibleUsers();

    // Assert
    assertEquals(1, requestCounter.get());

    ArgumentCaptor<AiInsightTask> taskCaptor = ArgumentCaptor.forClass(AiInsightTask.class);
    verify(aiInsightTaskRepo, times(2)).save(taskCaptor.capture());
    
    // First save is Status.PROCESSING
    AiInsightTask firstSave = taskCaptor.getAllValues().get(0);
    assertEquals(Status.PROCESSING, firstSave.getStatus());
    
    // Second save is Status.COMPLETED with insightText
    AiInsightTask secondSave = taskCaptor.getAllValues().get(1);
    assertEquals(Status.COMPLETED, secondSave.getStatus());
    assertEquals("Top expense is Groceries.", secondSave.getInsightText());

    verify(appUserRepo, times(1)).save(user);
    assertNotNull(user.getLastInsightAt());
  }

  @Test
  void shouldSkipGeneration_WhenRateLimitReached() {
    // Arrange
    requestCounter.set(13); // Limit is 13

    AppUser user = AppUser.builder()
        .id("user-123")
        .email("user@example.com")
        .build();

    when(appUserRepo.findUsersEligibleForInsights(anyLong(), eq(PageRequest.of(0, 13))))
        .thenReturn(List.of(user));

    // Act
    aiAnalyticsService.generateInsightsForEligibleUsers();

    // Assert
    assertEquals(13, requestCounter.get()); // Counter should remain 13
    verifyNoInteractions(aiInsightTaskRepo);
    verifyNoInteractions(transactionRepo);
    verifyNoInteractions(chatClient);
  }

  @Test
  void shouldMarkTaskAsFailed_WhenLlmCallThrowsException() {
    // Arrange
    AppUser user = AppUser.builder()
        .id("user-123")
        .email("user@example.com")
        .build();

    when(appUserRepo.findUsersEligibleForInsights(anyLong(), eq(PageRequest.of(0, 13))))
        .thenReturn(List.of(user));

    when(aiInsightTaskRepo.save(any(AiInsightTask.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(transactionRepo.findRecentTransactions(eq("user-123"), any(LocalDate.class)))
        .thenReturn(Collections.emptyList());

    when(chatClient.prompt(anyString())).thenReturn(requestSpec);
    when(requestSpec.system(anyString())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenThrow(new RuntimeException("LLM offline"));

    // Act
    aiAnalyticsService.generateInsightsForEligibleUsers();

    // Assert
    assertEquals(1, requestCounter.get());

    ArgumentCaptor<AiInsightTask> taskCaptor = ArgumentCaptor.forClass(AiInsightTask.class);
    verify(aiInsightTaskRepo, times(2)).save(taskCaptor.capture());
    
    AiInsightTask finalSave = taskCaptor.getAllValues().get(1);
    assertEquals(Status.FAILED, finalSave.getStatus());
    assertNull(user.getLastInsightAt());
  }
}
