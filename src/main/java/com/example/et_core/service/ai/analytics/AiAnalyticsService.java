package com.example.et_core.service.ai.analytics;

public interface AiAnalyticsService {
  void storeAiInsights(String userId);
  void generateInsightsForEligibleUsers();
}
