package com.example.et_core.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, boolean onboarded) {
}
