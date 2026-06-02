package com.example.et_core.service.userconfig;

import com.example.et_core.dto.UserConfigDto;
import com.example.et_core.model.UserConfig;

public interface UserConfigService {
  UserConfig getByUserId(String appUserId);

  UserConfigDto getConfig(String userId);

  UserConfigDto updateConfig(String userId, UserConfigDto dto);
}
