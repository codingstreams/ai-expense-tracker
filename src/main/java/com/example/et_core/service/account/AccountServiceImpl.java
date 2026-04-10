package com.example.et_core.service.account;

import com.example.et_core.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepo accountRepo;

    @Override
    public boolean existsByUserAndAccount(String appUserId, Long accountId) {
        return accountRepo.existsByAppUserIdAndAccountId(appUserId, accountId);
    }
}
