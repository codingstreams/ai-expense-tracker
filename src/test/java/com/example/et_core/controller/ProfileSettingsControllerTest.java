package com.example.et_core.controller;

import com.example.et_core.dto.*;
import com.example.et_core.model.*;
import com.example.et_core.repo.*;
import com.example.et_core.service.appuser.AppUserService;
import com.example.et_core.service.transaction.TransactionBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProfileSettingsControllerTest {

    @Autowired
    private AccountController accountController;

    @Autowired
    private UserConfigController userConfigController;

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

    private Bank savedBank;
    private PaymentMode defaultPaymentMode;
    private PaymentMode newPaymentMode;
    private String appUserId;

    @BeforeEach
    void setUp() {
        // Clear all data
        userConfigRepo.deleteAll();
        cardRepo.deleteAll();
        accountRepo.deleteAll();
        appUserRepo.deleteAll();
        bankRepo.deleteAll();
        paymentModeRepo.deleteAll();

        // Seed reference data
        savedBank = bankRepo.save(Bank.builder().name("ICICI Bank").build());
        defaultPaymentMode = paymentModeRepo.save(PaymentMode.builder()
            .name("Debit Card")
            .type(TransactionBehavior.ASSET)
            .build());
        newPaymentMode = paymentModeRepo.save(PaymentMode.builder()
            .name("Credit Card")
            .type(TransactionBehavior.LIABILITY)
            .build());

        // Register user
        appUserService.registerUser(new RegisterRequest("Alice", "alice@example.com", "password123"));
        AppUser appUser = appUserRepo.findAll().iterator().next();
        appUserId = appUser.getId();

        // Create a Savings Account
        Account savings = accountRepo.save(Account.builder()
            .bank(savedBank)
            .appUser(appUser)
            .balance(5000.0)
            .lastFourDigits("1111")
            .build());

        // Create a Cash Account
        Account cash = accountRepo.save(Account.builder()
            .bank(null)
            .appUser(appUser)
            .balance(200.0)
            .lastFourDigits("CASH")
            .build());

        // Create a Credit Card Card linked to the savings account
        cardRepo.save(Card.builder()
            .account(savings)
            .appUser(appUser)
            .cardType(CardType.CREDIT_CARD)
            .lastFourDigits("9999")
            .creditLimit(100000.0)
            .build());

        // Setup UserConfig
        userConfigRepo.save(UserConfig.builder()
            .appUser(appUser)
            .defaultPaymentMode(defaultPaymentMode)
            .defaultAccount(savings)
            .languagePreference(LanguagePreference.ENGLISH)
            .build());
    }

    @Test
    void shouldReturnConsolidatedLinkedAccountsAndCards() {
        final var response = accountController.getAllAccounts(appUserId);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        List<AccountDto> accounts = response.getBody();
        assertNotNull(accounts);
        // Expecting 3 accounts: Savings, Cash, Credit Card
        assertEquals(3, accounts.size());

        // Verify Savings Account
        AccountDto savingsDto = accounts.stream()
            .filter(a -> "Savings".equals(a.type()))
            .findFirst()
            .orElse(null);
        assertNotNull(savingsDto);
        assertEquals("ICICI Bank", savingsDto.bankName());
        assertEquals("1111", savingsDto.lastFour());
        assertEquals(5000.0, savingsDto.amount());

        // Verify Cash Account
        AccountDto cashDto = accounts.stream()
            .filter(a -> "Cash".equals(a.type()))
            .findFirst()
            .orElse(null);
        assertNotNull(cashDto);
        assertEquals("Wallet Cash", cashDto.bankName());
        assertEquals("0000", cashDto.lastFour());
        assertEquals(200.0, cashDto.amount());

        // Verify Credit Card
        AccountDto creditDto = accounts.stream()
            .filter(a -> "Credit".equals(a.type()))
            .findFirst()
            .orElse(null);
        assertNotNull(creditDto);
        assertEquals("ICICI Bank Credit Card", creditDto.bankName());
        assertEquals("9999", creditDto.lastFour());
        assertEquals(100000.0, creditDto.amount());
    }

    @Test
    void shouldGetAndPatchUserConfig() {
        // 1. Get current config
        var getResponse = userConfigController.getConfig(appUserId);
        assertNotNull(getResponse);
        assertEquals(200, getResponse.getStatusCode().value());

        UserConfigDto config = getResponse.getBody();
        assertNotNull(config);
        assertEquals("English", config.language());
        assertEquals(defaultPaymentMode.getId(), config.defaultPaymentModeId());

        // 2. Patch config (change language to Hindi and payment mode to Credit Card)
        var patchResponse = userConfigController.updateConfig(
            new UserConfigDto("Hindi", newPaymentMode.getId()),
            appUserId
        );
        assertNotNull(patchResponse);
        assertEquals(200, patchResponse.getStatusCode().value());

        UserConfigDto updatedConfig = patchResponse.getBody();
        assertNotNull(updatedConfig);
        assertEquals("Hindi", updatedConfig.language());
        assertEquals(newPaymentMode.getId(), updatedConfig.defaultPaymentModeId());

        // 3. Verify get config again
        var getResponse2 = userConfigController.getConfig(appUserId);
        assertEquals("Hindi", getResponse2.getBody().language());
        assertEquals(newPaymentMode.getId(), getResponse2.getBody().defaultPaymentModeId());
    }
}
