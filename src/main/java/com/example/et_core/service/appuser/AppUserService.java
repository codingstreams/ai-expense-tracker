package com.example.et_core.service.appuser;

import com.example.et_core.dto.RegisterRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AppUserService extends UserDetailsService {
  void registerUser(RegisterRequest request);
}
