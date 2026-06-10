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
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationService implements NotificationService {
  private static final int TIMEOUT_IN_MINUTES = 5;
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final Map<String, JobStatusDto> latestStatuses = new ConcurrentHashMap<>();

  @Override
  public void send(JobStatusDto jobStatus) {
    final var jobId = jobStatus.jobId();
    latestStatuses.put(jobId, jobStatus);

    final var emitter = emitters.get(jobId);
    if (emitter == null) {
      log.debug("No emitter found for jobId: {} yet. Status cached.", jobId);
      return;
    }

    try {
      emitter.send(jobStatus);
      if ("COMPLETED".equals(jobStatus.status()) || "FAILED".equals(jobStatus.status())) {
        log.info("Sending terminal status {} for jobId: {}. Completing emitter asynchronously.", jobStatus.status(), jobId);
        emitters.remove(jobId);
        latestStatuses.remove(jobId);
        completeEmitterDeferred(emitter);
      }
    } catch (IOException e) {
      log.error("Failed to send SSE event for jobId: {}. Removing emitter.", jobId, e);
      emitters.remove(jobId);
      latestStatuses.remove(jobId);
    }
  }

  @Override
  public void openConnection(String jobId) {
    // Clear any stale cached status
    latestStatuses.remove(jobId);

    final var emitter = new SseEmitter(Duration.ofMinutes(TIMEOUT_IN_MINUTES).toMillis());

    // Register lifecycle callbacks to prevent memory leaks
    emitter.onCompletion(() -> {
      log.debug("SSE emitter completed for jobId: {}", jobId);
      emitters.remove(jobId);
      latestStatuses.remove(jobId);
    });

    emitter.onTimeout(() -> {
      log.warn("SSE emitter timed out for jobId: {}", jobId);
      emitters.remove(jobId);
      latestStatuses.remove(jobId);
    });

    emitter.onError(ex -> {
      log.error("SSE emitter error for jobId: {}", jobId, ex);
      emitters.remove(jobId);
      latestStatuses.remove(jobId);
    });

    emitters.put(jobId, emitter);
  }

  @Override
  public SseEmitter get(String jobId) {
    final var cachedStatus = latestStatuses.get(jobId);

    if (cachedStatus != null && 
        ("COMPLETED".equals(cachedStatus.status()) || "FAILED".equals(cachedStatus.status()))) {
      log.info("Client requested status for completed job: {}. Returning immediate completed emitter.", jobId);
      final var tempEmitter = new SseEmitter(Duration.ofMinutes(TIMEOUT_IN_MINUTES).toMillis());
      try {
        tempEmitter.send(cachedStatus);
        completeEmitterDeferred(tempEmitter);
      } catch (IOException e) {
        log.error("Failed to send terminal status to temporary emitter for jobId: {}", jobId, e);
      }
      emitters.remove(jobId);
      latestStatuses.remove(jobId);
      return tempEmitter;
    }

    var emitter = emitters.get(jobId);
    if (emitter == null) {
      log.info("No pre-opened emitter found for jobId: {} on client connect. Creating a new one.", jobId);
      emitter = new SseEmitter(Duration.ofMinutes(TIMEOUT_IN_MINUTES).toMillis());
      
      final var finalEmitter = emitter;
      emitter.onCompletion(() -> {
        log.debug("SSE emitter completed for jobId: {}", jobId);
        emitters.remove(jobId);
        latestStatuses.remove(jobId);
      });
      emitter.onTimeout(() -> {
        log.warn("SSE emitter timed out for jobId: {}", jobId);
        emitters.remove(jobId);
        latestStatuses.remove(jobId);
      });
      emitter.onError(ex -> {
        log.error("SSE emitter error for jobId: {}", jobId, ex);
        emitters.remove(jobId);
        latestStatuses.remove(jobId);
      });
      
      emitters.put(jobId, emitter);
    }

    if (cachedStatus != null) {
      try {
        emitter.send(cachedStatus);
      } catch (IOException e) {
        log.error("Failed to send cached status to emitter for jobId: {}", jobId, e);
      }
    }

    return emitter;
  }

  @Override
  public void closeConnection(String jobId) {
    final var emitter = emitters.remove(jobId);
    latestStatuses.remove(jobId);
    if (emitter != null) {
      completeEmitterDeferred(emitter);
    }
  }

  private void completeEmitterDeferred(SseEmitter emitter) {
    if (emitter == null) return;
    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(500); // 500ms delay to allow Spring/Tomcat to flush the response cleanly
        emitter.complete();
      } catch (Exception ex) {
        // ignore
      }
    });
  }
}
