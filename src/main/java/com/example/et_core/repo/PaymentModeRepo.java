package com.example.et_core.repo;


import com.example.et_core.model.Account;
import com.example.et_core.model.PaymentMode;
import org.springframework.data.repository.CrudRepository;

public interface PaymentModeRepo extends CrudRepository<PaymentMode, Long> {
}
