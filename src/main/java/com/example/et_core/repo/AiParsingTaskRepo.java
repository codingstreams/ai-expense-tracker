package com.example.et_core.repo;

import com.example.et_core.model.AiParsingTask;
import com.example.et_core.model.Status;

import org.hibernate.query.spi.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AiParsingTaskRepo extends JpaRepository<AiParsingTask, Long> {
  List<AiParsingTask> findAllByStatusOrderByCreatedAtAsc(Status status, Limit limit);

  @Query("SELECT apt " +
      "FROM AiParsingTask apt " +
      "JOIN FETCH apt.appUser u " +
      "WHERE apt.status = :status " +
      "ORDER BY apt.createdAt ASC")
  List<AiParsingTask> findAllByStatusWithAppUserOrderByCreatedAtAsc(
      @Param("status") Status status,
      Limit limit);

  @Query("SELECT apt " +
      "FROM AiParsingTask apt " +
      "JOIN FETCH apt.appUser u " +
      "WHERE apt.id = :jobId")
  Optional<AiParsingTask> findByIdWithAppUser(Long jobId);

  @Query("SELECT apt " +
      "FROM AiParsingTask apt " +
      "WHERE apt.appUser.id = :appUserId AND apt.status IN :statuses " +
      "ORDER BY apt.createdAt ASC")
  List<AiParsingTask> findActiveTasks(
      @Param("appUserId") String appUserId,
      @Param("statuses") List<Status> statuses);
}
