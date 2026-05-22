package com.example.et_core.repo;

import com.example.et_core.model.AppUser;
import com.example.et_core.model.Transaction;
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
            .build();

        Transaction recentTx =Transaction.builder()
            .appUser(user)
            .transactionDate(LocalDate.parse("15-05-2024", FORMATTER))
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
        List<Transaction> results = transactionRepo.findAllByAppUserRecent(userId, pageable);

        // Assert
        assertThat(results).hasSize(3);
        
        // Verify the Order: 15-05-2024 should be first, 01-01-2023 last
        assertThat(results.get(0).getTransactionDate().format(FORMATTER)).isEqualTo("15-05-2024");
        assertThat(results.get(1).getTransactionDate().format(FORMATTER)).isEqualTo("10-02-2024");
        assertThat(results.get(2).getTransactionDate().format(FORMATTER)).isEqualTo("01-01-2023");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTransactions() {
        List<Transaction> results = transactionRepo.findAllByAppUserRecent("non-existent", PageRequest.of(0, 10));
        assertThat(results).isEmpty();
    }
}