package com.example.et_core.repo;

import com.example.et_core.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepo extends JpaRepository<AppUser, String> {
  boolean existsByEmail(String email);

  Optional<AppUser> findByEmail(String email);
}
