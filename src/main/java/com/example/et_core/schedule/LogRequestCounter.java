package com.example.et_core.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogRequestCounter {
  private final AtomicInteger requestCounter;

  @Scheduled(fixedRate = 60000)
  public void logAndRestRequestCounter(){
    log.info("Reset Counter. Request Processed: {}", requestCounter.getAndSet(0));
  }

}
