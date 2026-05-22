package com.example.et_core.controller;

import com.example.et_core.dto.PaymentModeResponseDto;
import com.example.et_core.repo.PaymentModeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/payment-modes")
@RequiredArgsConstructor
public class PaymentModeController {
    private final PaymentModeRepo paymentModeRepo;

    @GetMapping
    public ResponseEntity<List<PaymentModeResponseDto>> getSupportedPaymentModes() {
        final var paymentModes = StreamSupport.stream(paymentModeRepo.findAll().spliterator(), false)
            .map(pm -> new PaymentModeResponseDto(pm.getId(), pm.getName(), pm.getType()))
            .toList();
        return ResponseEntity.ok(paymentModes);
    }
}
