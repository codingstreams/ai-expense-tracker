package com.example.et_core.service.ai;

import com.example.et_core.dto.AiInputDto;
import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
  private final TransactionMapper transactionMapper;

  private static final String SYSTEM_PROMPT = """
      Rules:
      1. Your job is to parse the raw text from the user which is either related to expense or income.
      
      2. Based on the type of text decide the 'type' field of output json. Allowed values for 'type' fields are EXPENSE or INCOME.
      
      3. Field 'transactionDate' is having date format: dd-mm-yyyy
      
      4. If the user uses relative dates (e.g., 'today', 'yesterday'). Get the date from following: 
        - Current Year for reference: {year}
        - If Today then use {today}
        - If Yesterday then use {yesterday}
        - Day before yesterday then use {dayBeforeYesterday}
        - If no date then use {today}
        - If date mentioned in raw text then pick that date.
      
      5. Extract description from raw text and don't change or add anything to it.
      
      6. Extract amount from raw text and don't change or add anything to it. Just convert the string to double representation.
      
      7. Don't answer anything not related to expense or income related raw text. Simply set the 'errorMessage' field of the output json with "NOT_VALID_INPUT"
      
      8. Raw Text is in English or Hindi language only.
      """;

  private final ChatClient chatClient;

  @Override
  public TransactionRequestDto parse(AiInputDto requestBody) {

    final var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    final var sysPromptVars = new HashMap<String, Object>();
    final var now = LocalDateTime.now();
    final var today = formatter.format(now);
    final var yesterday = formatter.format(now.minusDays(1));
    final var dayBeforeYesterday = formatter.format(now.minusDays(2));

    sysPromptVars.put("today", today);
    sysPromptVars.put("yesterday", yesterday);
    sysPromptVars.put("dayBeforeYesterday", dayBeforeYesterday);
    sysPromptVars.put("year", now.getYear());

    final var sysPrompt = SystemPromptTemplate.builder()
        .template(SYSTEM_PROMPT)
        .variables(sysPromptVars)
        .build();

    final var aiParseResult = chatClient.prompt(requestBody.rawText())
        .system(sysPrompt.render())
        .call()
        .entity(AiParseResult.class);

    return transactionMapper.fromAiParseResult(aiParseResult);
  }
}
