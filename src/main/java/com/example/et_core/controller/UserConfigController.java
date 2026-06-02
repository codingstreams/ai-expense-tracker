package com.example.et_core.controller;

import com.example.et_core.dto.UserConfigDto;
import com.example.et_core.service.userconfig.UserConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/config")
@RequiredArgsConstructor
public class UserConfigController {
    private final UserConfigService userConfigService;

    @GetMapping
    public ResponseEntity<UserConfigDto> getConfig(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userConfigService.getConfig(userId));
    }

    @PatchMapping
    public ResponseEntity<UserConfigDto> updateConfig(
        @RequestBody UserConfigDto dto,
        @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(userConfigService.updateConfig(userId, dto));
    }
}
