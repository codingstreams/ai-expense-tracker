package com.example.et_core.service.ai;

import com.example.et_core.dto.AiInputDto;
import com.example.et_core.dto.TransactionRequestDto;

public interface AiService {
  TransactionRequestDto parse(AiInputDto requestBody);
}
