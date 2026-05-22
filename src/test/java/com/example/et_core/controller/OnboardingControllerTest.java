package com.example.et_core.controller;

import com.example.et_core.dto.LoginRequest;
import com.example.et_core.dto.OnboardingRequestDto;
import com.example.et_core.dto.RegisterRequest;
import com.example.et_core.exception.UserAlreadyOnboardedException;
import com.example.et_core.model.Bank;
import com.example.et_core.model.CardType;
import com.example.et_core.model.LanguagePreference;
import com.example.et_core.model.PaymentMode;
import com.example.et_core.repo.*;
import com.example.et_core.service.appuser.AppUserService;
import com.example.et_core.service.auth.AuthService;
import com.example.et_core.service.transaction.TransactionBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OnboardingControllerTest {

    @Autowired
    private OnboardingController onboardingController;

    @Autowired
    private BankController bankController;

    @Autowired
    private PaymentModeController paymentModeController;

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private PaymentModeRepo paymentModeRepo;

    @Autowired
    private AppUserRepo appUserRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private CardRepo cardRepo;

    @Autowired
    private UserConfigRepo userConfigRepo;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AuthService authService;

    private Bank savedBank;
    private PaymentMode savedPaymentMode;
    private String userEmail = "onboarding.test2@example.com";
    private String userPassword = "password123";
    private String appUserId;

    @BeforeEach
    void setUp() {
        // Clean up database tables in correct order
        userConfigRepo.deleteAll();
        cardRepo.deleteAll();
        accountRepo.deleteAll();
        appUserRepo.deleteAll();
        bankRepo.deleteAll();
        paymentModeRepo.deleteAll();

        // Seed bank and payment mode
        savedBank = bankRepo.save(Bank.builder().name("State Bank of India").build());
        savedPaymentMode = paymentModeRepo.save(PaymentMode.builder()
            .name("UPI")
            .type(TransactionBehavior.ASSET)
            .build());

        // Register user
        appUserService.registerUser(new RegisterRequest("Test User", userEmail, userPassword));
        final var userOpt = appUserRepo.findAll().iterator();
        assertTrue(userOpt.hasNext());
        appUserId = userOpt.next().getId();

        // Authenticate user to get JWT token
        final var authResponse = authService.login(new LoginRequest(userEmail, userPassword));
        assertFalse(authResponse.onboarded(), "User should not be onboarded initially");
    }

    @Test
    void shouldSuccessfullyOnboardUser_andPreventSubsequentOnboarding() {
        // Prepare onboarding request DTO
        final var onboardingRequest = new OnboardingRequestDto(
            savedBank.getId(),
            "4321",
            15000.0,
            CardType.DEBIT_CARD,
            "9876",
            50000.0,
            1200.0,
            savedPaymentMode.getId(),
            LanguagePreference.ENGLISH
        );

        // Perform onboarding request directly on controller
        final var response = onboardingController.onboard(onboardingRequest, appUserId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        // Verify Bank Account
        final var accounts = accountRepo.findAll();
        int accountCount = 0;
        com.example.et_core.model.Account bankAccount = null;
        com.example.et_core.model.Account cashAccount = null;
        for (var account : accounts) {
            accountCount++;
            if ("CASH".equals(account.getLastFourDigits())) {
                cashAccount = account;
            } else {
                bankAccount = account;
            }
        }
        assertEquals(2, accountCount, "Should have created exactly 2 accounts");
        assertNotNull(bankAccount);
        assertNotNull(cashAccount);

        assertEquals("4321", bankAccount.getLastFourDigits());
        assertEquals(15000.0, bankAccount.getBalance());
        assertEquals(savedBank.getId(), bankAccount.getBank().getId());
        assertEquals(appUserId, bankAccount.getAppUser().getId());

        // Verify Cash Account
        assertEquals("CASH", cashAccount.getLastFourDigits());
        assertEquals(1200.0, cashAccount.getBalance());
        assertNull(cashAccount.getBank());
        assertEquals(appUserId, cashAccount.getAppUser().getId());

        // Verify Card details
        final var cards = cardRepo.findAll();
        assertTrue(cards.iterator().hasNext(), "Card should be created");
        final var card = cards.iterator().next();
        assertEquals("9876", card.getLastFourDigits());
        assertEquals(CardType.DEBIT_CARD, card.getCardType());
        assertEquals(50000.0, card.getCreditLimit());
        assertEquals(bankAccount.getId(), card.getAccount().getId());

        // Verify UserConfig details
        final var userConfigOpt = userConfigRepo.findByAppUserId(appUserId);
        assertTrue(userConfigOpt.isPresent(), "UserConfig should exist");
        final var userConfig = userConfigOpt.get();
        assertEquals(savedPaymentMode.getId(), userConfig.getDefaultPaymentMode().getId());
        assertEquals(bankAccount.getId(), userConfig.getDefaultAccount().getId());
        assertEquals(LanguagePreference.ENGLISH, userConfig.getLanguagePreference());

        // Verify subsequent login shows user as onboarded
        final var secondaryAuthResponse = authService.login(new LoginRequest(userEmail, userPassword));
        assertTrue(secondaryAuthResponse.onboarded(), "User should now be marked as onboarded");

        // Verify that submitting onboarding request again fails with UserAlreadyOnboardedException
        assertThrows(UserAlreadyOnboardedException.class, () -> {
            onboardingController.onboard(onboardingRequest, appUserId);
        });
    }

    @Test
    void shouldReturnSupportedBanks() {
        final var response = bankController.getSupportedBanks();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        final var banks = response.getBody();
        assertNotNull(banks);
        assertEquals(1, banks.size());
        assertEquals("State Bank of India", banks.get(0).getName());
    }

    @Test
    void shouldReturnSupportedPaymentModes() {
        final var response = paymentModeController.getSupportedPaymentModes();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        final var paymentModes = response.getBody();
        assertNotNull(paymentModes);
        assertEquals(1, paymentModes.size());
        assertEquals("UPI", paymentModes.get(0).name());
        assertEquals(TransactionBehavior.ASSET, paymentModes.get(0).type());
    }
}
