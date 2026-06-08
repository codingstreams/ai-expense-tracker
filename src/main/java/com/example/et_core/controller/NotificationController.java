package com.example.et_core.controller;

import com.example.et_core.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping("/status/{jobId}")
  public ResponseEntity<SseEmitter> connect(@PathVariable String jobId){
    final var emitter = notificationService.get(jobId);

    return ResponseEntity.ok(emitter);
  }
}
