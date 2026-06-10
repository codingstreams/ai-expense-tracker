package com.example.et_core.service.notification;

import com.example.et_core.dto.JobStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationService implements NotificationService {
  private static final int TIMEOUT_IN_MINUTES = 5;
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  @Override
  public void send(JobStatusDto jobStatus) {
    final var emitter = emitters.get(jobStatus.jobId());

    if (emitter == null) {
      log.warn("No emitter found for jobId: {}. Client may have disconnected.", jobStatus.jobId());
      return;
    }

    try {
      emitter.send(jobStatus);
    } catch (IOException e) {
      log.error("Failed to send SSE event for jobId: {}. Removing emitter.", jobStatus.jobId(), e);
      emitters.remove(jobStatus.jobId());
    }
  }

  @Override
  public void openConnection(String jobId) {
    final var emitter = new SseEmitter(Duration.ofMinutes(TIMEOUT_IN_MINUTES).toMillis());

    // Register lifecycle callbacks to prevent memory leaks
    emitter.onCompletion(() -> {
      log.debug("SSE emitter completed for jobId: {}", jobId);
      emitters.remove(jobId);
    });

    emitter.onTimeout(() -> {
      log.warn("SSE emitter timed out for jobId: {}", jobId);
      emitters.remove(jobId);
    });

    emitter.onError(ex -> {
      log.error("SSE emitter error for jobId: {}", jobId, ex);
      emitters.remove(jobId);
    });

    emitters.put(jobId, emitter);
  }

  @Override
  public SseEmitter get(String jobId) {
    final var emitter = emitters.get(jobId);

    if (emitter == null) {
      throw new RuntimeException("No emitter found for jobId: " + jobId);
    }

    return emitter;
  }

  @Override
  public void closeConnection(String jobId) {
    final var emitter = emitters.remove(jobId);

    if (emitter != null) {
      emitter.complete();
    } else {
      log.warn("Attempted to close non-existent SSE connection for jobId: {}", jobId);
    }
  }
}
