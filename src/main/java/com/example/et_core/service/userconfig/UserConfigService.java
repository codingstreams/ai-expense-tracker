package com.example.et_core.service.userconfig;

import com.example.et_core.model.UserConfig;

public interface UserConfigService {
  UserConfig getByUserId(String appUserId);
}
