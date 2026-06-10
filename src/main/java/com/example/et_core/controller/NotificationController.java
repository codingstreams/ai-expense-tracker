package com.example.et_core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.et_core.service.notifications.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping("/subscribe")
  public ResponseEntity<SseEmitter> connect(
      @RequestParam("sessionId") String sessionId,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(notificationService.openConnection(userId, sessionId));
  }
}
