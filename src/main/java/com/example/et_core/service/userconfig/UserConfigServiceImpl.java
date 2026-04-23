package com.example.et_core.service.userconfig;

import com.example.et_core.model.UserConfig;
import com.example.et_core.repo.UserConfigRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConfigServiceImpl implements UserConfigService {
  private final UserConfigRepo userConfigRepo;

  @Override
  public UserConfig getByUserId(String appUserId) {
    return userConfigRepo.findByAppUserId(appUserId)
        .orElseThrow(() -> new RuntimeException("UserConfig not found for appUserId:" + appUserId));
  }
}
