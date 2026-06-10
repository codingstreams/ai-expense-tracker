package com.example.et_core.repo;

import com.example.et_core.model.SystemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemCategoryRepo extends JpaRepository<SystemCategory, Long> {
    Optional<SystemCategory> findByName(String name);
}
