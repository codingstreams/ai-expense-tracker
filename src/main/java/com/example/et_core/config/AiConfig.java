package com.example.et_core.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AiConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.defaultOptions(
            ChatOptions.builder()
                .model("llama3.2:latest")
                .temperature(0.2)
                .build()
        )
        .build();

  }

  @Bean
  AtomicInteger requestCounter(){
    return new AtomicInteger(0);
  }
}
