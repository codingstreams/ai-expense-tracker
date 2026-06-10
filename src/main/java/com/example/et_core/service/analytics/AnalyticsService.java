package com.example.et_core.service.analytics;

import com.example.et_core.dto.AiInsightResponseDto;
import com.example.et_core.dto.CategoryDistributionDto;
import com.example.et_core.dto.DailyCashFlowProjection;
import com.example.et_core.dto.MonthlyCashFlowProjection;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
  List<DailyCashFlowProjection> getDailyCashFlow(String userId, LocalDate startDate, LocalDate endDate);
  List<MonthlyCashFlowProjection> getMonthlyCashFlow(String userId, LocalDate startDate, LocalDate endDate);
  List<AiInsightResponseDto> getAiInsights(String userId);
  List<CategoryDistributionDto> getCategoryDistribution(String userId, LocalDate startDate, LocalDate endDate);
}
