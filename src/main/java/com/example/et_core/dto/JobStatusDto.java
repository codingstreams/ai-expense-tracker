package com.example.et_core.dto;

import java.time.LocalDateTime;

public record JobStatusDto (String jobId, String status, String timestamp){
  public static JobStatusDto of(String jobId, String status){
    return new JobStatusDto(jobId, status, LocalDateTime.now().toString());
  }
}
