package com.example.et_core.service.onboarding;

import com.example.et_core.dto.OnboardingRequestDto;
import com.example.et_core.exception.BankNotFoundException;
import com.example.et_core.exception.PaymentModeNotFoundException;
import com.example.et_core.exception.UserAlreadyOnboardedException;
import com.example.et_core.model.*;
import com.example.et_core.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {
    private final UserConfigRepo userConfigRepo;
    private final BankRepo bankRepo;
    private final AccountRepo accountRepo;
    private final CardRepo cardRepo;
    private final PaymentModeRepo paymentModeRepo;
    private final AppUserRepo appUserRepo;

    @Override
    @Transactional
    public void onboard(String userId, OnboardingRequestDto request) {
        // Check if already onboarded
        if (userConfigRepo.findByAppUserId(userId).isPresent()) {
            throw new UserAlreadyOnboardedException(userId);
        }

        // Fetch User and update onboarding status
        final var appUser = appUserRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Validate Bank
        final var bank = bankRepo.findById(request.bankId())
            .orElseThrow(() -> new BankNotFoundException(request.bankId()));

        // Validate Payment Mode
        final var paymentMode = paymentModeRepo.findById(request.defaultPaymentModeId())
            .orElseThrow(() -> new PaymentModeNotFoundException(request.defaultPaymentModeId()));

        // 1. Create primary bank account
        final var bankAccount = Account.builder()
            .bank(bank)
            .appUser(appUser)
            .balance(request.bankBalance())
            .lastFourDigits(request.lastFourDigits())
            .build();
        final var savedBankAccount = accountRepo.save(bankAccount);

        // 2. Setup Card Details if provided
        if (request.cardType() != null && request.cardLastFourDigits() != null) {
            final var card = Card.builder()
                .account(savedBankAccount)
                .cardType(request.cardType())
                .lastFourDigits(request.cardLastFourDigits())
                .creditLimit(request.cardLimit() != null ? request.cardLimit() : 0.0)
                .build();
            cardRepo.save(card);
        }

        // 3. Create Cash Account
        final var cashAccount = Account.builder()
            .bank(null)
            .appUser(appUser)
            .balance(request.cashBalance() != null ? request.cashBalance() : 0.0)
            .lastFourDigits("CASH")
            .build();
        accountRepo.save(cashAccount);

        // 4. Create UserConfig
        final var userConfig = UserConfig.builder()
            .appUser(appUser)
            .defaultPaymentMode(paymentMode)
            .defaultAccount(savedBankAccount)
            .languagePreference(request.languagePreference() != null ? request.languagePreference() : LanguagePreference.ENGLISH)
            .build();
        userConfigRepo.save(userConfig);

        // 5. Update and save user onboarding complete flag
        appUser.setOnboardingComplete(true);
        appUserRepo.save(appUser);
    }
}
