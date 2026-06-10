package com.example.et_core.service.ai.parsetask;

import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Status;
import com.example.et_core.repo.AiParsingTaskRepo;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.spi.Limit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiParseTaskServiceImpl implements  AiParseTaskService{
  private final AiParsingTaskRepo repo;
  @Override
  public AiParsingTask save(AiParsingTask aiParsingTask) {
    return repo.save(aiParsingTask);
  }

  @Override
  public List<AiParsingTask> getPendingTasks(Status status) {
    final var limit = new Limit();
    limit.setMaxRows(13);

    return repo.findAllByStatusOrderByCreatedAtAsc(status, limit);
  }

  @Override
  public List<AiParsingTask> getPendingTasksWithAppUser(Status status) {
    final var limit = new Limit();
    limit.setMaxRows(13);

    return repo.findAllByStatusWithAppUserOrderByCreatedAtAsc(status, limit);
  }

  @Override
  public AiParsingTask getByIdWithAppUser(Long jobId) {
    return repo.findByIdWithAppUser(jobId)
        .orElseThrow(()-> new RuntimeException("Ai Parse Task not found with id: " + jobId));
  }

  @Override
  public List<AiParsingTask> getActiveTasks(String appUserId) {
    return repo.findActiveTasks(appUserId, List.of(Status.PENDING, Status.PROCESSING));
  }
}
