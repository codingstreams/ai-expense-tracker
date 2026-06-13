package com.example.et_core.repo;

import com.example.et_core.model.UserConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserConfigRepo extends CrudRepository<UserConfig, Long> {
    Optional<UserConfig> findByAppUserId(String appUserId);
}
