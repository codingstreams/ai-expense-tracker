package com.example.et_core.service.analytics;

import com.example.et_core.dto.AiInsightResponseDto;
import com.example.et_core.dto.CategoryDistributionDto;
import com.example.et_core.dto.DailyCashFlowProjection;
import com.example.et_core.dto.MonthlyCashFlowProjection;
import com.example.et_core.repo.AiInsightTaskRepo;
import com.example.et_core.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
  private final TransactionRepo transactionRepo;
  private final AiInsightTaskRepo aiInsightTaskRepo;

  @Override
  public List<DailyCashFlowProjection> getDailyCashFlow(String userId, LocalDate startDate, LocalDate endDate) {
    return transactionRepo.findCashFlowDaily(userId, startDate, endDate);
  }

  @Override
  public List<MonthlyCashFlowProjection> getMonthlyCashFlow(String userId, LocalDate startDate, LocalDate endDate) {
    return transactionRepo.findCashFlowMonthly(userId, startDate, endDate);
  }

  @Override
  public List<AiInsightResponseDto> getAiInsights(String userId) {
    return aiInsightTaskRepo.findAllByAppUserIdOrderByCreatedAtDesc(userId).stream()
        .map(task -> new AiInsightResponseDto(
            task.getId(),
            task.getType().name(),
            task.getInsightText(),
            task.getCreatedAt(),
            task.getStatus().name()
        ))
        .collect(Collectors.toList());
  }

  @Override
  public List<CategoryDistributionDto> getCategoryDistribution(String userId, LocalDate startDate, LocalDate endDate) {
    return transactionRepo.findCategorySpend(userId, startDate, endDate).stream()
        .map(row -> {
            String label = (String) row[0];
            Double amount = (Double) row[1];
            Double limit = getDefaultLimit(label);
            return new CategoryDistributionDto(label, amount != null ? amount : 0.0, limit);
        })
        .collect(Collectors.toList());
  }

  private Double getDefaultLimit(String label) {
    if (label == null) return 5000.0;
    return switch (label.toLowerCase()) {
      case "food & drinks", "food" -> 6000.0;
      case "entertainment" -> 3000.0;
      case "shopping" -> 10000.0;
      case "travel" -> 2500.0;
      default -> 5000.0;
    };
  }
}
