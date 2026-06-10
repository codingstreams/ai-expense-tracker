package com.example.et_core.service.ai.analytics;

import com.example.et_core.model.AiInsightTask;
import com.example.et_core.model.AppUser;
import com.example.et_core.model.InsightType;
import com.example.et_core.model.Status;
import com.example.et_core.model.Transaction;
import com.example.et_core.repo.AiInsightTaskRepo;
import com.example.et_core.repo.AppUserRepo;
import com.example.et_core.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalyticsServiceImpl implements AiAnalyticsService {
  private final AppUserRepo appUserRepo;
  private final AiInsightTaskRepo aiInsightTaskRepo;
  private final TransactionRepo transactionRepo;
  private final ChatClient chatClient;
  private final AtomicInteger requestCounter;

  private static final String SYSTEM_PROMPT = """
      Rules:
      1. Your job is to analyze the user's transaction history for the past 7 days and provide concise, actionable spending/income insights.
      
      2. Identify top expense categories, unexpected spikes, or patterns.
      
      3. Provide practical saving tips based on their spending.
      
      4. Keep the summary engaging, helpful, and concise. Format it using Markdown (bullet points, bold text).
      
      5. If there are no transactions, provide a friendly encouragement to start tracking their expenses.
      """;

  private static final long ONE_WEEK_IN_MS = 7L * 24 * 60 * 60 * 1000;

  @Override
  public void storeAiInsights(String userId) {
    final var appUser = appUserRepo.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    var task = AiInsightTask.builder()
        .appUser(appUser)
        .type(InsightType.WEEKLY)
        .status(Status.PENDING)
        .build();
    aiInsightTaskRepo.save(task);
  }

  @Override
  public void generateInsightsForEligibleUsers() {
    log.info("START - generateInsightsForEligibleUsers");
    long cutoffTime = System.currentTimeMillis() - ONE_WEEK_IN_MS;

    // Fetch up to 13 eligible users in a batch
    List<AppUser> eligibleUsers = appUserRepo.findUsersEligibleForInsights(cutoffTime, PageRequest.of(0, 13));
    log.info("Found {} users eligible for insights", eligibleUsers.size());

    for (int i = 0; i < eligibleUsers.size(); i++) {
      AppUser user = eligibleUsers.get(i);

      int currentCount = requestCounter.get();
      if (currentCount >= 13) {
        log.warn("Rate limit reached ({} requests). Skipping remaining users for this minute.", currentCount);
        break;
      }

      requestCounter.incrementAndGet();
      log.info("Generating insights for user: {} (Request counter: {})", user.getEmail(), requestCounter.get());

      AiInsightTask task = AiInsightTask.builder()
          .appUser(user)
          .type(InsightType.WEEKLY)
          .status(Status.PROCESSING)
          .build();
      task = aiInsightTaskRepo.save(task);

      try {
        List<Transaction> transactions = transactionRepo.findRecentTransactions(user.getId(), LocalDate.now().minusDays(7));

        String transactionSummary = transactions.stream()
            .map(t -> String.format("- Date: %s, Type: %s, Amount: %.2f, Category: %s, Description: %s",
                t.getTransactionDate(),
                t.getType(),
                t.getAmount(),
                t.getCategoryName() != null ? t.getCategoryName() : "None",
                t.getDescription() != null ? t.getDescription() : ""))
            .collect(Collectors.joining("\n"));

        final var sysPrompt = SystemPromptTemplate.builder()
            .template(SYSTEM_PROMPT)
            .build();

        String userPrompt = "Here is my transaction history for the past 7 days:\n" + transactionSummary;

        String insightText = chatClient.prompt(userPrompt)
            .system(sysPrompt.render())
            .call()
            .content();

        task.setInsightText(insightText);
        task.setStatus(Status.COMPLETED);
        aiInsightTaskRepo.save(task);

        user.setLastInsightAt(System.currentTimeMillis());
        appUserRepo.save(user);

        log.info("Successfully generated insights for user: {}", user.getEmail());
      } catch (Exception e) {
        log.error("Failed to generate insights for user: {}", user.getEmail(), e);
        task.setStatus(Status.FAILED);
        aiInsightTaskRepo.save(task);
      }

      // Sleep for 4.5 seconds between requests if there are more users remaining in the batch
      if (i < eligibleUsers.size() - 1) {
        try {
          Thread.sleep(4500);
        } catch (InterruptedException e) {
          log.warn("Insight generation loop interrupted.");
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    log.info("END - generateInsightsForEligibleUsers");
  }
}
