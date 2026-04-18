package com.example.et_core.service.notification;

import com.example.et_core.dto.JobStatusDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
  void send(JobStatusDto jobStatus);

  void openConnection(String string);

  SseEmitter get(String jobId);

  void closeConnection(String jobId);
}
