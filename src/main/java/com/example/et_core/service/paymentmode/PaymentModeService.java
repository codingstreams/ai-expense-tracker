package com.example.et_core.service.paymentmode;

import com.example.et_core.model.PaymentMode;

public interface PaymentModeService {
    boolean existsById(Long paymentModeId);

  PaymentMode get(Long paymentModeId);
}
