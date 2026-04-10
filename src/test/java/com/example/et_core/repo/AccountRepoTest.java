package com.example.et_core.repo;

import com.example.et_core.model.Account;
import com.example.et_core.model.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepoTest {
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AppUserRepo appUserRepo;

    @Test
    void shouldReturnTrue_whenUserAndAccountMatch() {
        final var appUser = AppUser.builder()
                .build();

        final var savedAppUser = appUserRepo.save(appUser);

        final var account = Account.builder()
                .appUser(savedAppUser)
                .build();

        final var savedAccount = accountRepo.save(account);

        final var appUserId = savedAppUser.getId();
        final var accountId = savedAccount.getId();

        assertTrue(accountRepo.existsByAppUserIdAndAccountId(appUserId, accountId));
    }

    @Test
    void shouldReturnFalse_whenUserAndAccountNotMatch() {
        final var appUser = AppUser.builder()
                .build();

        final var savedAppUser = appUserRepo.save(appUser);

        final var account = Account.builder()
                .appUser(savedAppUser)
                .build();

        final var savedAccount = accountRepo.save(account);

        final var appUserId = UUID.randomUUID().toString();
        final var accountId = savedAccount.getId();

        assertFalse(accountRepo.existsByAppUserIdAndAccountId(appUserId, accountId));
    }
}