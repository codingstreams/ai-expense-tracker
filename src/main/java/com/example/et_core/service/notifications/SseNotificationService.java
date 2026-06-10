package com.example.et_core.service.notifications;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SseNotificationService implements NotificationService {
  private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down SseNotificationService, closing all active SSE emitters...");
    emitters.forEach((key, emitter) -> {
      try {
        emitter.complete();
      } catch (Exception e) {
        log.warn("Failed to complete emitter for session {}", key, e);
      }
    });
    emitters.clear();
  }

  // 10 minutes timeout
  private final static Long TIMEOUT = 10L * 60 * 1000;

  @Override
  public SseEmitter openConnection(String userId, String sessionId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT);
    emitters.put(userId + ":" + sessionId, emitter);

    emitter.onCompletion(() -> emitters.remove(userId + ":" + sessionId));
    emitter.onTimeout(() -> emitters.remove(userId + ":" + sessionId));
    emitter.onError((e) -> emitters.remove(userId + ":" + sessionId));

    try {
      emitter.send(SseEmitter.event().name(NotificationEvent.CONNECTED.getEvent())
          .data("Connected to Backend Notification Service"));
    } catch (IOException e) {
      log.error("Failed to open connection for user {}", userId, e);
      emitters.remove(userId + ":" + sessionId);
    }

    return emitter;
  }

  @Override
  public void send(String userId, String sessionId, NotificationEvent event, Object data) {
    if (sessionId != null) {
      SseEmitter emitter = emitters.get(userId + ":" + sessionId);
      if (emitter != null) {
        try {
          emitter.send(SseEmitter.event().name(event.getEvent()).data(data));
        } catch (IOException e) {
          emitters.remove(userId + ":" + sessionId);
        }
      }
    } else {
      // Broadcast to all active sessions of this user
      emitters.forEach((key, emitter) -> {
        if (key.startsWith(userId + ":")) {
          try {
            emitter.send(SseEmitter.event().name(event.getEvent()).data(data));
          } catch (IOException e) {
            emitters.remove(key);
          }
        }
      });
    }
  }

  @Override
  public void closeConnection(String userId, String sessionId) {
    SseEmitter emitter = emitters.get(userId + ":" + sessionId);
    if (emitter != null) {
      emitter.complete();
      emitters.remove(userId + ":" + sessionId);
    }
  }
}
