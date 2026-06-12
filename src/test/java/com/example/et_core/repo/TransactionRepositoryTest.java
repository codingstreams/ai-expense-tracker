package com.example.et_core.repo;

import com.example.et_core.dto.DailyCashFlowProjection;
import com.example.et_core.dto.MonthlyCashFlowProjection;
import com.example.et_core.model.AppUser;
import com.example.et_core.model.Transaction;
import com.example.et_core.model.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private AppUserRepo appUserRepo;

    private String userId;

    @BeforeEach
    void setUp() {
        // 1. Setup User
        AppUser user = new AppUser();
        appUserRepo.save(user);
        userId = user.getId();

        Transaction oldTx = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.parse("01-01-2023", FORMATTER))
                .type(TransactionType.INCOME)
                .build();

        Transaction recentTx = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.parse("15-05-2024", FORMATTER))
                .type(TransactionType.EXPENSE)
                .build();

        Transaction middleTx = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.parse("10-02-2024", FORMATTER))
                .build();

        transactionRepo.save(oldTx);
        transactionRepo.save(recentTx);
        transactionRepo.save(middleTx);
    }

    @AfterEach
    void cleanup() {
        transactionRepo.deleteAll();
        appUserRepo.deleteAll();
    }

    @Test
    void shouldFindAllTransactionsByUserIdOrderedByDateDesc() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        List<Transaction> results = transactionRepo.findAllByAppUserIdOrderByTransactionDateDesc(userId, pageable);

        // Assert
        assertThat(results).hasSize(3);

        // Verify the Order: 15-05-2024 should be first, 01-01-2023 last
        assertThat(results.get(0).getTransactionDate().format(FORMATTER)).isEqualTo("15-05-2024");
        assertThat(results.get(1).getTransactionDate().format(FORMATTER)).isEqualTo("10-02-2024");
        assertThat(results.get(2).getTransactionDate().format(FORMATTER)).isEqualTo("01-01-2023");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTransactions() {
        List<Transaction> results = transactionRepo.findAllByAppUserIdOrderByTransactionDateDesc("non-existent", PageRequest.of(0, 10));
        assertThat(results).isEmpty();
    }

    @Test
    void shouldFindCashFlowDailyWithinRange() {
        // Arrange - Setup a user and some specific transactions
        AppUser user = new AppUser();
        var savedUser = appUserRepo.save(user);
        String testUserId = savedUser.getId();

        Transaction tx1 = Transaction.builder()
                .appUser(savedUser)
                .transactionDate(LocalDate.of(2026, 6, 1))
                .type(TransactionType.INCOME)
                .amount(1000.0)
                .build();

        Transaction tx2 = Transaction.builder()
                .appUser(savedUser)
                .transactionDate(LocalDate.of(2026, 6, 1))
                .type(TransactionType.EXPENSE)
                .amount(200.0)
                .build();

        Transaction tx3 = Transaction.builder()
                .appUser(savedUser)
                .transactionDate(LocalDate.of(2026, 6, 2))
                .type(TransactionType.INCOME)
                .amount(500.0)
                .build();

        Transaction tx4 = Transaction.builder()
                .appUser(savedUser)
                .transactionDate(LocalDate.of(2026, 6, 3))
                .type(TransactionType.EXPENSE)
                .amount(300.0)
                .build();

        // Transaction outside date range
        Transaction txOutside = Transaction.builder()
                .appUser(savedUser)
                .transactionDate(LocalDate.of(2026, 6, 5))
                .type(TransactionType.INCOME)
                .amount(100.0)
                .build();

        // Transaction with TRANSFER type (should not sum under INCOME/EXPENSE as they
        // are distinct types, but check the projection handling)
        Transaction txTransfer = Transaction.builder()
                .appUser(savedUser)
                .transactionDate(LocalDate.of(2026, 6, 1))
                .type(TransactionType.TRANSFER)
                .amount(150.0)
                .build();

        // transactionRepo.saveAll(List.of(tx1, tx2, tx3, tx4, txOutside, txTransfer));

        transactionRepo.save(tx1);
        transactionRepo.save(tx2);
        transactionRepo.save(tx3);
        transactionRepo.save(tx4);
        transactionRepo.save(txOutside);
        transactionRepo.save(txTransfer);

        // Act
        List<DailyCashFlowProjection> results = transactionRepo.findCashFlowDaily(
                testUserId,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3));

        // Assert
        assertThat(results).hasSize(3);

        // Ordered by date ASC
        assertThat(results.get(0).getTransactionDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(results.get(0).getIncome()).isEqualTo(1000.0);
        assertThat(results.get(0).getExpense()).isEqualTo(200.0);

        assertThat(results.get(1).getTransactionDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(results.get(1).getIncome()).isEqualTo(500.0);
        assertThat(results.get(1).getExpense()).isEqualTo(0.0);

        assertThat(results.get(2).getTransactionDate()).isEqualTo(LocalDate.of(2026, 6, 3));
        assertThat(results.get(2).getIncome()).isEqualTo(0.0);
        assertThat(results.get(2).getExpense()).isEqualTo(300.0);
    }

    @Test
    void shouldFindCashFlowMonthlyWithinRange() {
        // Arrange
        AppUser user = new AppUser();
        appUserRepo.save(user);
        String testUserId = user.getId();

        Transaction tx1 = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.of(2025, 12, 15))
                .type(TransactionType.INCOME)
                .amount(2000.0)
                .build();

        Transaction tx2 = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.of(2026, 1, 10))
                .type(TransactionType.INCOME)
                .amount(5000.0)
                .build();

        Transaction tx3 = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.of(2026, 1, 20))
                .type(TransactionType.EXPENSE)
                .amount(1500.0)
                .build();

        Transaction tx4 = Transaction.builder()
                .appUser(user)
                .transactionDate(LocalDate.of(2026, 2, 5))
                .type(TransactionType.EXPENSE)
                .amount(800.0)
                .build();

        transactionRepo.saveAll(List.of(tx1, tx2, tx3, tx4));

        // Act
        List<MonthlyCashFlowProjection> results = transactionRepo.findCashFlowMonthly(
                testUserId,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2026, 2, 28));

        // Assert
        assertThat(results).hasSize(3);

        // Ordered by Year and Month ASC
        assertThat(results.get(0).getYear()).isEqualTo(2025);
        assertThat(results.get(0).getMonth()).isEqualTo(12);
        assertThat(results.get(0).getIncome()).isEqualTo(2000.0);
        assertThat(results.get(0).getExpense()).isEqualTo(0.0);

        assertThat(results.get(1).getYear()).isEqualTo(2026);
        assertThat(results.get(1).getMonth()).isEqualTo(1);
        assertThat(results.get(1).getIncome()).isEqualTo(5000.0);
        assertThat(results.get(1).getExpense()).isEqualTo(1500.0);

        assertThat(results.get(2).getYear()).isEqualTo(2026);
        assertThat(results.get(2).getMonth()).isEqualTo(2);
        assertThat(results.get(2).getIncome()).isEqualTo(0.0);
        assertThat(results.get(2).getExpense()).isEqualTo(800.0);
    }
}
