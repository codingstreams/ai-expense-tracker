package com.example.et_core.service.ai;

import com.example.et_core.dto.AiActiveTaskDto;
import com.example.et_core.dto.AiInputDto;
import com.example.et_core.dto.AiTaskDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.model.AiParsingTask;

import java.util.List;

public interface AiService {
  TransactionRequestDto parse(AiInputDto requestBody);

  void parse(AiParsingTask task);

  AiTaskDto save(String appUserId, AiInputDto requestBody);

  List<AiActiveTaskDto> getActiveTasks(String appUserId);
}
