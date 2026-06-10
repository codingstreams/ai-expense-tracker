package com.example.et_core.service.notifications;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
  SseEmitter openConnection(String userId, String sessionId);

  void send(String userId, String sessionId, NotificationEvent event, Object data);

  void closeConnection(String userId, String sessionId);

  static enum NotificationEvent {
    // Connection
    CONNECTED("CONNECTED"),

    // Transaction
    TRANSACTION_CREATED("TRANSACTION_CREATED"),
    TRANSACTION_UPDATED("TRANSACTION_UPDATED"),
    TRANSACTION_DELETED("TRANSACTION_DELETED"),

    // Category
    CATEGORY_CREATED("CATEGORY_CREATED"),
    CATEGORY_UPDATED("CATEGORY_UPDATED"),
    CATEGORY_DELETED("CATEGORY_DELETED"),

    // Account
    ACCOUNT_CREATED("ACCOUNT_CREATED"),
    ACCOUNT_UPDATED("ACCOUNT_UPDATED"),
    ACCOUNT_DELETED("ACCOUNT_DELETED"),

    // User
    USER_CREATED("USER_CREATED"),
    USER_UPDATED("USER_UPDATED"),
    USER_DELETED("USER_DELETED"),

    // AI
    AI_TASK_CREATED("AI_TASK_CREATED"),
    AI_TASK_PROCESSING("AI_TASK_PROCESSING"),
    AI_TASK_COMPLETED("AI_TASK_COMPLETED"),
    AI_TASK_FAILED("AI_TASK_FAILED"),
    AI_TASK_ERROR("AI_TASK_ERROR"),
    AI_TASK_QUEUED("AI_TASK_QUEUED");

    private final String event;

    NotificationEvent(String event) {
      this.event = event;
    }

    public String getEvent() {
      return event;
    }
  }

}
