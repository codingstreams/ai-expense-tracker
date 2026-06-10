package com.example.et_core.schedule;

import com.example.et_core.service.ai.analytics.AiAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiInsightScheduler {
  private final AiAnalyticsService aiAnalyticsService;

  @Scheduled(cron = "0 * 2-5 * * *", zone = "Asia/Kolkata")
  public void runInsightScheduler() {
    log.info("Running scheduled AI Insight generation task");
    aiAnalyticsService.generateInsightsForEligibleUsers();
  }
}
