package com.example.et_core.repo;



import com.example.et_core.model.Bank;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BankRepo extends CrudRepository<Bank, Long> {
    Optional<Bank> findByName(String stateBankOfIndia);
}
