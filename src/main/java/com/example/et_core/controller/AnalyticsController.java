package com.example.et_core.controller;

import com.example.et_core.dto.AiInsightResponseDto;
import com.example.et_core.dto.CategoryDistributionDto;
import com.example.et_core.dto.DailyCashFlowProjection;
import com.example.et_core.dto.MonthlyCashFlowProjection;
import com.example.et_core.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
  private final AnalyticsService analyticsService;

  @GetMapping("/cashflow/daily")
  public ResponseEntity<List<DailyCashFlowProjection>> getDailyCashFlow(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @AuthenticationPrincipal String userId
  ) {
    return ResponseEntity.ok(analyticsService.getDailyCashFlow(userId, startDate, endDate));
  }

  @GetMapping("/cashflow/monthly")
  public ResponseEntity<List<MonthlyCashFlowProjection>> getMonthlyCashFlow(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @AuthenticationPrincipal String userId
  ) {
    return ResponseEntity.ok(analyticsService.getMonthlyCashFlow(userId, startDate, endDate));
  }

  @GetMapping("/insights")
  public ResponseEntity<List<AiInsightResponseDto>> getAiInsights(
      @AuthenticationPrincipal String userId
  ) {
    return ResponseEntity.ok(analyticsService.getAiInsights(userId));
  }

  @GetMapping("/category-distribution")
  public ResponseEntity<List<CategoryDistributionDto>> getCategoryDistribution(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @AuthenticationPrincipal String userId
  ) {
    return ResponseEntity.ok(analyticsService.getCategoryDistribution(userId, startDate, endDate));
  }
}
