package com.example.et_core.service.paymentmode;

import com.example.et_core.exception.PaymentModeNotFoundException;
import com.example.et_core.model.PaymentMode;
import com.example.et_core.repo.PaymentModeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentModeServiceImpl implements PaymentModeService {
    private final PaymentModeRepo paymentModeRepo;

    @Override
    public boolean existsById(Long paymentModeId) {
        return paymentModeRepo.existsById(paymentModeId);
    }

    @Override
    public PaymentMode get(Long paymentModeId) {
        return paymentModeRepo.findById(paymentModeId)
            .orElseThrow(()->new PaymentModeNotFoundException(paymentModeId));
    }
}
