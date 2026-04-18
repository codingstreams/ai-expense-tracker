package com.example.et_core.controller;

import com.example.et_core.dto.AiInputDto;
import com.example.et_core.dto.AiTaskDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-input")
@RequiredArgsConstructor
public class AiController {
  private static final String LOGGED_IN_USER = "f6f2435f-08ac-4b8d-a705-8449ac607685";

  private final AiService aiService;

  @PostMapping
  public ResponseEntity<AiTaskDto> parseRawText(@RequestBody AiInputDto requestBody){
    final var response = aiService.save(LOGGED_IN_USER, requestBody);

    return  ResponseEntity.ok(response);
  }
}
