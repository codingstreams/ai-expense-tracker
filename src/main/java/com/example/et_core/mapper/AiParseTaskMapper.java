package com.example.et_core.mapper;

import com.example.et_core.dto.AiTaskDto;
import com.example.et_core.model.AiParsingTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AiParseTaskMapper {
  AiParseTaskMapper INSTANCE = Mappers.getMapper(AiParseTaskMapper.class);

  @Mapping(source = "message", target = "message")
  AiTaskDto toDto(AiParsingTask aiParsingTask, String message);
}
