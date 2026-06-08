package com.example.et_core.service.auth;

import com.example.et_core.dto.AuthResponse;
import com.example.et_core.dto.LoginRequest;

public interface AuthService {
  AuthResponse login(LoginRequest request);
}
