package com.example.et_core.repo;

import com.example.et_core.model.PaymentMode;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PaymentModeRepo extends CrudRepository<PaymentMode, Long> {
    Optional<PaymentMode> findByName(String name);
}
