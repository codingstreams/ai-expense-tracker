package com.example.et_core.repo;

import com.example.et_core.model.*;
import com.example.et_core.specification.TransactionSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionSpecificationTest {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private AppUserRepo appUserRepo;

    @Autowired
    private SystemCategoryRepo systemCategoryRepo;

    @Autowired
    private UserCategoryRepo userCategoryRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private PaymentModeRepo paymentModeRepo;

    private String userId1;
    private String userId2;

    private SystemCategory foodSysCategory;
    private UserCategory shoppingUserCategory;

    private Account cashAccount;
    private PaymentMode cashPaymentMode;

    @BeforeEach
    void setUp() {
        // Create users
        AppUser user1 = new AppUser();
        user1 = appUserRepo.save(user1);
        userId1 = user1.getId();

        AppUser user2 = new AppUser();
        user2 = appUserRepo.save(user2);
        userId2 = user2.getId();

        // Create categories
        foodSysCategory = SystemCategory.builder()
                .name("Food")
                .build();
        foodSysCategory = systemCategoryRepo.save(foodSysCategory);

        shoppingUserCategory = UserCategory.builder()
                .name("Shopping")
                .appUser(user1)
                .build();
        shoppingUserCategory = userCategoryRepo.save(shoppingUserCategory);

        // Create account
        cashAccount = Account.builder()
                .balance(1000.0)
                .appUser(user1)
                .build();
        cashAccount = accountRepo.save(cashAccount);

        // Create payment mode
        cashPaymentMode = PaymentMode.builder()
                .name("Cash")
                .build();
        cashPaymentMode = paymentModeRepo.save(cashPaymentMode);

        // Save transactions for user1
        Transaction t1 = Transaction.builder()
                .appUser(user1)
                .amount(-100.0) // Expense of 100
                .transactionDate(LocalDate.of(2026, 6, 1))
                .type(TransactionType.EXPENSE)
                .description("Burger lunch")
                .systemCategory(foodSysCategory)
                .account(cashAccount)
                .paymentMode(cashPaymentMode)
                .build();

        Transaction t2 = Transaction.builder()
                .appUser(user1)
                .amount(500.0) // Income of 500
                .transactionDate(LocalDate.of(2026, 6, 5))
                .type(TransactionType.INCOME)
                .description("Freelance project pay")
                .account(cashAccount)
                .paymentMode(cashPaymentMode)
                .build();

        Transaction t3 = Transaction.builder()
                .appUser(user1)
                .amount(-200.0) // Expense of 200
                .transactionDate(LocalDate.of(2026, 6, 10))
                .type(TransactionType.EXPENSE)
                .description("New jeans shopping")
                .userCategory(shoppingUserCategory)
                .account(cashAccount)
                .paymentMode(cashPaymentMode)
                .build();

        // Save transaction for user2
        Transaction t4 = Transaction.builder()
                .appUser(user2)
                .amount(-300.0)
                .transactionDate(LocalDate.of(2026, 6, 5))
                .type(TransactionType.EXPENSE)
                .description("User 2 grocery")
                .build();

        transactionRepo.saveAll(List.of(t1, t2, t3, t4));
    }

    @AfterEach
    void cleanup() {
        transactionRepo.deleteAll();
        accountRepo.deleteAll();
        userCategoryRepo.deleteAll();
        systemCategoryRepo.deleteAll();
        paymentModeRepo.deleteAll();
        appUserRepo.deleteAll();
    }

    @Test
    void shouldFilterByUserAndIsolateTenant() {
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, null, null, null, null, null, null, null, null, null
        );

        Page<Transaction> result = transactionRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(t -> t.getAppUser().getId().equals(userId1));
    }

    @Test
    void shouldFilterByDateRange() {
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 10),
                null, null, null, null, null, null, null
        );

        Page<Transaction> result = transactionRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Freelance project pay", "New jeans shopping");
    }

    @Test
    void shouldFilterByAmountRangeUsingAbsoluteValue() {
        // minAmount 150, maxAmount 600
        // Burgers (abs(-100) = 100) -> Excluded
        // Freelance (abs(500) = 500) -> Included
        // Jeans (abs(-200) = 200) -> Included
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, null, null, 150.0, 600.0, null, null, null, null, null
        );

        Page<Transaction> result = transactionRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Freelance project pay", "New jeans shopping");
    }

    @Test
    void shouldFilterByTypes() {
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, null, null, null, null, List.of(TransactionType.INCOME), null, null, null, null
        );

        Page<Transaction> result = transactionRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Freelance project pay");
    }

    @Test
    void shouldFilterByCategoryIds() {
        // system category foodSysCategory (positive ID)
        // user category shoppingUserCategory (negative ID in filter)
        Long foodCatId = foodSysCategory.getId();
        Long shoppingCatId = -shoppingUserCategory.getId();

        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, null, null, null, null, null, List.of(foodCatId, shoppingCatId), null, null, null
        );

        Page<Transaction> result = transactionRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Burger lunch", "New jeans shopping");
    }

    @Test
    void shouldFilterBySearchTermCaseInsensitively() {
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, null, null, null, null, null, null, null, null, "luNCh"
        );

        Page<Transaction> result = transactionRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Burger lunch");
    }

    @Test
    void shouldPaginateAndSort() {
        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                userId1, null, null, null, null, null, null, null, null, null
        );

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate"));

        Page<Transaction> result = transactionRepo.findAll(spec, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        // Page 0 should have the two most recent: Jeans (June 10) and Freelance (June 5)
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("New jeans shopping");
        assertThat(result.getContent().get(1).getDescription()).isEqualTo("Freelance project pay");
    }
}
