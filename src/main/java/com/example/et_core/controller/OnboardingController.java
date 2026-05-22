package com.example.et_core.controller;

import com.example.et_core.dto.OnboardingRequestDto;
import com.example.et_core.service.onboarding.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {
    private final OnboardingService onboardingService;

    @PostMapping
    public ResponseEntity<Void> onboard(
        @RequestBody OnboardingRequestDto request,
        @AuthenticationPrincipal String userId
    ) {
        onboardingService.onboard(userId, request);
        return ResponseEntity.ok().build();
    }
}
