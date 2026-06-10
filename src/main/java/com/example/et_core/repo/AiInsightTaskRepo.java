package com.example.et_core.repo;

import com.example.et_core.model.AiInsightTask;
import com.example.et_core.model.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiInsightTaskRepo extends JpaRepository<AiInsightTask, Long> {
    List<AiInsightTask> findAllByStatus(Status status, Pageable pageable);
    List<AiInsightTask> findAllByAppUserIdOrderByCreatedAtDesc(String appUserId);
}
