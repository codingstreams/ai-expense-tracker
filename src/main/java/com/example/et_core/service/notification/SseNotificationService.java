package com.example.et_core.service.notification;

import com.example.et_core.dto.JobStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseNotificationService implements NotificationService {
  private static final int TIMEOUT_IN_MINUTES = 5;
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  @Override
  public void send(JobStatusDto jobStatus) {
    final var emitter = this.get(jobStatus.jobId());

    try {
      emitter.send(jobStatus);
    } catch (IOException e) {
      throw new RuntimeException("Unable to send job status to emitter for jobId: " + jobStatus.jobId(), e);
    }
  }

  @Override
  public void openConnection(String jobId) {
    final var emitter = new SseEmitter(Duration.ofMinutes(TIMEOUT_IN_MINUTES).toMillis());
    emitters.put(jobId, emitter);
  }

  @Override
  public SseEmitter get(String jobId) {
    final var emitter = emitters.get(jobId);

    if (Objects.isNull(emitter)) {
      throw new RuntimeException("No emitter found for jobId: " + jobId);
    }

    return emitter;
  }

  @Override
  public void closeConnection(String jobId) {
    this.get(jobId).complete();
  }
}
