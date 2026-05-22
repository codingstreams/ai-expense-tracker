package com.example.et_core.service.onboarding;

import com.example.et_core.dto.OnboardingRequestDto;

public interface OnboardingService {
    void onboard(String userId, OnboardingRequestDto request);
}
