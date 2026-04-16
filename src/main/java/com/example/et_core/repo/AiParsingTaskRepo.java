package com.example.et_core.repo;

import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Status;

import org.hibernate.query.spi.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiParsingTaskRepo extends JpaRepository<AiParsingTask, Long> {
  List<AiParsingTask> findAllByStatusOrderByCreatedAtAsc(Status status, Limit limit);
}
