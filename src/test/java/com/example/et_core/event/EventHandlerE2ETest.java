package com.example.et_core.event;

import com.example.et_core.dto.RegisterRequest;
import com.example.et_core.model.*;
import com.example.et_core.repo.*;
import com.example.et_core.service.appuser.AppUserService;
import com.example.et_core.service.notification.NotificationService;
import com.example.et_core.service.transaction.TransactionBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EventHandlerE2ETest {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private TransactionRepo transactionRepo;

  @Autowired
  private AiParsingTaskRepo aiParsingTaskRepo;

  @Autowired
  private UserConfigRepo userConfigRepo;

  @Autowired
  private CardRepo cardRepo;

  @Autowired
  private AccountRepo accountRepo;

  @Autowired
  private UserCategoryRepo userCategoryRepo;

  @Autowired
  private AppUserRepo appUserRepo;

  @Autowired
  private BankRepo bankRepo;

  @Autowired
  private PaymentModeRepo paymentModeRepo;

  @Autowired
  private SystemCategoryRepo systemCategoryRepo;

  @Autowired
  private AppUserService appUserService;

  @MockitoSpyBean
  private NotificationService notificationService;

  private AppUser savedUser;
  private Bank savedBank;
  private PaymentMode savedPaymentMode;
  private SystemCategory savedCategory;
  private Account savedAccount;

  @BeforeEach
  void setUp() {
    // Teardown database in order to prevent foreign key constraint violations
    transactionRepo.deleteAll();
    aiParsingTaskRepo.deleteAll();
    userConfigRepo.deleteAll();
    cardRepo.deleteAll();
    accountRepo.deleteAll();
    userCategoryRepo.deleteAll();
    appUserRepo.deleteAll();
    bankRepo.deleteAll();
    paymentModeRepo.deleteAll();
    systemCategoryRepo.deleteAll();

    // 1. Seed base references
    savedBank = bankRepo.save(Bank.builder().name("State Bank of India").build());
    savedPaymentMode = paymentModeRepo.save(PaymentMode.builder()
        .name("UPI")
        .type(TransactionBehavior.ASSET)
        .build());
    savedCategory = systemCategoryRepo.save(SystemCategory.builder().name("Groceries").build());

    // 2. Register user
    appUserService.registerUser(new RegisterRequest("Bob", "bob@example.com", "password123"));
    savedUser = appUserRepo.findAll().iterator().next();

    // 3. Create Account
    savedAccount = accountRepo.save(Account.builder()
        .bank(savedBank)
        .appUser(savedUser)
        .balance(1000.0)
        .lastFourDigits("1234")
        .build());

    // 4. Create UserConfig
    userConfigRepo.save(UserConfig.builder()
        .appUser(savedUser)
        .defaultPaymentMode(savedPaymentMode)
        .defaultAccount(savedAccount)
        .languagePreference(LanguagePreference.ENGLISH)
        .build());
  }

  @Test
  void shouldProcessCompletedAiParsingTaskEvent_AndSaveTransaction() throws Exception {
    // 1. Save AiParsingTask
    AiParsingTask task = aiParsingTaskRepo.save(AiParsingTask.builder()
        .appUser(savedUser)
        .content("{\"type\":\"EXPENSE\",\"description\":\"Weekly Groceries Shopping\",\"amount\":120.5,\"date\":\"10-06-2026\",\"category\":\"Groceries\"}")
        .status(Status.COMPLETED)
        .build());

    // 2. Open Notification SSE connection so emitter is registered in-memory
    notificationService.openConnection(task.getId().toString());

    // 3. Publish the completed event
    AiParsingTaskCompleted event = new AiParsingTaskCompleted(task.getId(), task);
    eventPublisher.publishEvent(event);

    // 4. Poll/Wait for transaction to be created in DB (since event listener execution is @Async)
    List<Transaction> transactions = List.of();
    int retries = 50;
    while (retries > 0) {
      transactions = transactionRepo.findAllByAppUser(savedUser.getId());
      if (!transactions.isEmpty()) {
        break;
      }
      Thread.sleep(100);
      retries--;
    }

    // 5. Assertions on Database Transaction
    assertFalse(transactions.isEmpty(), "Transaction should be saved via async event handler");
    Transaction savedTxn = transactions.get(0);
    assertEquals("Weekly Groceries Shopping", savedTxn.getDescription());
    assertEquals(-120.5, savedTxn.getAmount()); // mapped type EXPENSE => negative amount
    assertEquals(savedAccount.getId(), savedTxn.getAccount().getId());
    assertEquals(savedPaymentMode.getId(), savedTxn.getPaymentMode().getId());
    assertEquals(savedCategory.getId(), savedTxn.getSystemCategory().getId());

    // 6. Verify notification and connection closed
    verify(notificationService, timeout(5000)).send(argThat(dto -> 
        dto != null && task.getId().toString().equals(dto.jobId()) && "COMPLETED".equals(dto.status())
    ));
    verify(notificationService, timeout(5000)).closeConnection(task.getId().toString());
  }
}
