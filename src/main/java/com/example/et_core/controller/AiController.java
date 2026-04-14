package com.example.et_core.controller;

import com.example.et_core.dto.AiInputDto;
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
  private final AiService aiService;

  @PostMapping
  public ResponseEntity<TransactionRequestDto> parseRawText(@RequestBody AiInputDto requestBody){
    final var response = aiService.parse(requestBody);

    return  ResponseEntity.ok(response);
  }
}
