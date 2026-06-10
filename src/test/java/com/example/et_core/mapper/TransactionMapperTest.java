package com.example.et_core.mapper;

import com.example.et_core.dto.TransactionRequestDto;
import com.example.et_core.model.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

  @Test
  void shouldReturnTransaction_whenValidExpenseTransactionRequest_isPresent() {
    final Long transactionId = null;
    final var type = "EXPENSE";
    final var description = "200 rs petrol";
    final var amount = 200.0;
    final var transactionDate = "10-04-2026";
    final var paymentModeId = 1L;
    final var accountId = 1L;
    final var categoryId = 1L;
    final Long toAccountId = null;

    final var dto = new TransactionRequestDto(
        transactionId,
        type,
        description,
        amount,
        transactionDate,
        paymentModeId,
        accountId,
        categoryId,
        toAccountId,
        null
    );

    final var transaction = new Transaction();
    TransactionMapper.INSTANCE.transactionFromRequestDto(dto, transaction, "testUser", null, false);

    assertEquals(TransactionType.valueOf(type), transaction.getType());
    assertEquals(description, transaction.getDescription());
    assertEquals(-amount, transaction.getAmount());
    assertEquals(java.time.LocalDate.parse(transactionDate, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")), transaction.getTransactionDate());
    assertEquals(PaymentMode.ofId(paymentModeId), transaction.getPaymentMode());
    assertEquals(Account.ofId(accountId), transaction.getAccount());
    assertEquals(SystemCategory.ofId(categoryId), transaction.getSystemCategory());
  }

  @Test
  void shouldReturnTransaction_whenValidIncomeTransactionRequest_isPresent() {
    final Long transactionId = null;
    final var type = "INCOME";
    final var description = "cashback";
    final var amount = 200.0;
    final var transactionDate = "10-04-2026";
    final var paymentModeId = 1L;
    final var accountId = 1L;
    final var categoryId = 1L;
    final Long toAccountId = null;

    final var dto = new TransactionRequestDto(
        transactionId,
        type,
        description,
        amount,
        transactionDate,
        paymentModeId,
        accountId,
        categoryId,
        toAccountId,
        null
    );

    final var transaction = new Transaction();
    TransactionMapper.INSTANCE.transactionFromRequestDto(dto, transaction, "testUser", null, false);

    assertEquals(TransactionType.valueOf(type), transaction.getType());
    assertEquals(description, transaction.getDescription());
    assertEquals(amount, transaction.getAmount());
    assertEquals(java.time.LocalDate.parse(transactionDate, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")), transaction.getTransactionDate());
    assertEquals(PaymentMode.ofId(paymentModeId), transaction.getPaymentMode());
    assertEquals(Account.ofId(accountId), transaction.getAccount());
    assertEquals(SystemCategory.ofId(categoryId), transaction.getSystemCategory());
  }

  @Test
  void shouldReturnTransaction_whenValidTransferTransactionRequestWithSourceAcc_isPresent() {
    final Long transactionId = null;
    final var type = "TRANSFER";
    final var description = "transfer";
    final var amount = 200.0;
    final var transactionDate = "10-04-2026";
    final var paymentModeId = 1L;
    final var accountId = 1L;
    final var categoryId = 1L;
    final Long toAccountId = 2L;

    final var dto = new TransactionRequestDto(
        transactionId,
        type,
        description,
        amount,
        transactionDate,
        paymentModeId,
        accountId,
        categoryId,
        toAccountId,
        null
    );

    final var transaction = new Transaction();
    TransactionMapper.INSTANCE.transactionFromRequestDto(dto, transaction, "testUser", null, true);

    assertEquals(TransactionType.valueOf(type), transaction.getType());
    assertEquals(description, transaction.getDescription());
    assertEquals(-amount, transaction.getAmount());
    assertEquals(java.time.LocalDate.parse(transactionDate, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")), transaction.getTransactionDate());
    assertEquals(PaymentMode.ofId(paymentModeId), transaction.getPaymentMode());
    assertEquals(Account.ofId(accountId), transaction.getAccount());
    assertEquals(SystemCategory.ofId(categoryId), transaction.getSystemCategory());
  }

  @Test
  void shouldReturnTransaction_whenValidTransferTransactionRequestWithTargetAcc_isPresent() {
    final Long transactionId = null;
    final var type = "TRANSFER";
    final var description = "transfer";
    final var amount = 200.0;
    final var transactionDate = "10-04-2026";
    final var paymentModeId = 1L;
    final var accountId = 1L;
    final var categoryId = 1L;
    final Long toAccountId = 2L;

    final var dto = new TransactionRequestDto(
        transactionId,
        type,
        description,
        amount,
        transactionDate,
        paymentModeId,
        accountId,
        categoryId,
        toAccountId,
        null
    );

    final var transferId = UUID.randomUUID().toString();

    final var transaction = new Transaction();
    TransactionMapper.INSTANCE.transactionFromRequestDto(dto, transaction, "testUser", transferId, false);

    assertEquals(TransactionType.valueOf(type), transaction.getType());
    assertEquals(description, transaction.getDescription());
    assertEquals(amount, transaction.getAmount());
    assertEquals(java.time.LocalDate.parse(transactionDate, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")), transaction.getTransactionDate());
    assertEquals(PaymentMode.ofId(paymentModeId), transaction.getPaymentMode());
    assertEquals(Account.ofId(toAccountId), transaction.getAccount());
    assertEquals(SystemCategory.ofId(categoryId), transaction.getSystemCategory());
    assertEquals(transferId, transaction.getTransferId());
  }



}