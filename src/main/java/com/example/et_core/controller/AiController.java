package com.example.et_core.controller;

import com.example.et_core.dto.AiInputDto;
import com.example.et_core.dto.AiTaskDto;
import com.example.et_core.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-input")
@RequiredArgsConstructor
public class AiController {
  private final AiService aiService;

  @PostMapping
  public ResponseEntity<AiTaskDto> parseRawText(@RequestBody AiInputDto requestBody,
      @AuthenticationPrincipal String userId) {
    final var response = aiService.save(userId, requestBody);

    return ResponseEntity.ok(response);
  }
}
