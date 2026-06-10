package com.example.et_core.repo;

import com.example.et_core.model.AppUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepo extends JpaRepository<AppUser, String> {
  boolean existsByEmail(String email);

  Optional<AppUser> findByEmail(String email);

  @Query("SELECT u FROM AppUser u " +
         "WHERE (u.lastInsightAt IS NULL OR u.lastInsightAt < :cutoffTime) " +
         "AND NOT EXISTS (SELECT 1 FROM AiInsightTask t WHERE t.appUser = u AND t.status IN (com.example.et_core.model.Status.PENDING, com.example.et_core.model.Status.PROCESSING))")
  List<AppUser> findUsersEligibleForInsights(@Param("cutoffTime") Long cutoffTime, Pageable pageable);
}

