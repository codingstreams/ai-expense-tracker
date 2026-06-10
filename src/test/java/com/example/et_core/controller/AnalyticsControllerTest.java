package com.example.et_core.controller;

import com.example.et_core.dto.AiInsightResponseDto;
import com.example.et_core.dto.DailyCashFlowProjection;
import com.example.et_core.dto.MonthlyCashFlowProjection;
import com.example.et_core.service.analytics.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

  @Mock
  private AnalyticsService analyticsService;

  private AnalyticsController analyticsController;

  @BeforeEach
  void setUp() {
    analyticsController = new AnalyticsController(analyticsService);
  }

  @Test
  void shouldReturnDailyCashFlow() {
    // Arrange
    LocalDate start = LocalDate.now().minusDays(7);
    LocalDate end = LocalDate.now();
    String userId = "user-123";
    DailyCashFlowProjection projection = mock(DailyCashFlowProjection.class);
    when(analyticsService.getDailyCashFlow(userId, start, end)).thenReturn(List.of(projection));

    // Act
    ResponseEntity<List<DailyCashFlowProjection>> response = analyticsController.getDailyCashFlow(start, end, userId);

    // Assert
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().size());
    verify(analyticsService).getDailyCashFlow(userId, start, end);
  }

  @Test
  void shouldReturnMonthlyCashFlow() {
    // Arrange
    LocalDate start = LocalDate.now().minusMonths(3);
    LocalDate end = LocalDate.now();
    String userId = "user-123";
    MonthlyCashFlowProjection projection = mock(MonthlyCashFlowProjection.class);
    when(analyticsService.getMonthlyCashFlow(userId, start, end)).thenReturn(List.of(projection));

    // Act
    ResponseEntity<List<MonthlyCashFlowProjection>> response = analyticsController.getMonthlyCashFlow(start, end, userId);

    // Assert
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().size());
    verify(analyticsService).getMonthlyCashFlow(userId, start, end);
  }

  @Test
  void shouldReturnAiInsights() {
    // Arrange
    String userId = "user-123";
    AiInsightResponseDto dto = new AiInsightResponseDto(1L, "WEEKLY", "Spending was high.", 12345678L, "COMPLETED");
    when(analyticsService.getAiInsights(userId)).thenReturn(List.of(dto));

    // Act
    ResponseEntity<List<AiInsightResponseDto>> response = analyticsController.getAiInsights(userId);

    // Assert
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().size());
    verify(analyticsService).getAiInsights(userId);
  }
}
