package com.example.et_core.service.transaction;

import com.example.et_core.dto.CreateTransactionDto;
import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.UpdateTransactionDto;
import com.example.et_core.exception.*;
import com.example.et_core.mapper.TransactionMapper;
import com.example.et_core.model.*;
import com.example.et_core.repo.TransactionRepo;
import com.example.et_core.service.account.AccountService;
import com.example.et_core.service.appuser.AppUserService;
import com.example.et_core.service.category.CategoryService;
import com.example.et_core.service.paymentmode.PaymentModeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {
  private final AppUserService appUserService;
  private final AccountService accountService;
  private final CategoryService categoryService;
  private final PaymentModeService paymentModeService;
  private final TransactionRepo transactionRepo;
  private final TransactionMapper transactionMapper;

  @Transactional
  @Override
  public TransactionDto saveTransaction(String appUserId, CreateTransactionDto requestBody) throws InsufficientAccountBalanceException {
    final var accountId = requestBody.accountId();
    final var categoryId = requestBody.categoryId();
    final var paymentModeId = requestBody.paymentModeId();
    final var toAccountId = requestBody.toAccountId();
    final var type = requestBody.type();

    final var accounts = getAccounts(accountId, toAccountId, type);

    validateAccountCategoryAndPaymentMode(appUserId, accounts, categoryId, paymentModeId);

    if (TransactionType.valueOf(type) == TransactionType.TRANSFER) {
      return handleTransfer(appUserId, requestBody, accountId, toAccountId, type, categoryId, paymentModeId);
    }

    return handleExpenseOrIncome(appUserId, requestBody, accountId, type, categoryId, paymentModeId);
  }

  private TransactionDto handleTransfer(String appUserId, CreateTransactionDto requestBody, Long accountId, Long toAccountId, String type, Long categoryId, Long paymentModeId) throws InsufficientAccountBalanceException {

    accountService.updateBalance(accountId, requestBody.amount(), requestBody.paymentModeId(), type, true);

    accountService.updateBalance(toAccountId, requestBody.amount(), requestBody.paymentModeId(), type, false);

    final var transferId = UUID.randomUUID().toString();

    final var debitTransaction = Transaction.builder()
        .appUser(AppUser.ofId(appUserId))
        .account(Account.ofId(accountId))
        .category(Category.ofId(categoryId))
        .paymentMode(PaymentMode.ofId(paymentModeId))
        .amount(-requestBody.amount())
        .transactionDate(requestBody.transactionDate())
        .description(requestBody.description())
        .type(TransactionType.valueOf(type))
        .transferId(transferId)
        .build();

    final var creditTransaction = Transaction.builder()
        .appUser(AppUser.ofId(appUserId))
        .account(Account.ofId(toAccountId))
        .category(Category.ofId(categoryId))
        .paymentMode(PaymentMode.ofId(paymentModeId))
        .amount(requestBody.amount())
        .transactionDate(requestBody.transactionDate())
        .description(requestBody.description())
        .type(TransactionType.valueOf(type))
        .transferId(transferId)
        .build();

    transactionRepo.save(debitTransaction);
    final var savedTransaction = transactionRepo.save(creditTransaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  private TransactionDto handleExpenseOrIncome(String appUserId, CreateTransactionDto requestBody, Long accountId, String type, Long categoryId, Long paymentModeId) throws InsufficientAccountBalanceException {
    accountService.updateBalance(accountId, requestBody.amount(), requestBody.paymentModeId(), type, false);

    final var transactionType = TransactionType.valueOf(type);
    final var transaction = Transaction.builder()
        .appUser(AppUser.ofId(appUserId))
        .account(Account.ofId(accountId))
        .category(Category.ofId(categoryId))
        .paymentMode(PaymentMode.ofId(paymentModeId))
        .amount(transactionType == TransactionType.EXPENSE ? -requestBody.amount() : requestBody.amount())
        .transactionDate(requestBody.transactionDate())
        .description(requestBody.description())
        .type(transactionType)
        .build();

    final var savedTransaction = transactionRepo.save(transaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  private static List<Long> getAccounts(Long accountId, Long toAccountId, String type) {
    List<Long> accounts = new ArrayList<>();
    accounts.add(accountId);

    if (TransactionType.valueOf(type) == TransactionType.TRANSFER) {
      accounts.add(toAccountId);
    }
    return accounts;
  }

  private void validateAccountCategoryAndPaymentMode(String appUserId, List<Long> accounts, Long categoryId, Long paymentModeId) {
    final var accountExists = accountService.existsByUserAndAccount(appUserId, accounts);

    if (!accountExists) {
      throw new AccountNotOwnedByUserException(accounts, appUserId);
    }

    final var categoryExists = categoryService.existsByUserAndCategory(appUserId, categoryId);

    if (!categoryExists) {
      throw new CategoryNotFoundException(categoryId);
    }

    final var paymentModeExists = paymentModeService.existsById(paymentModeId);

    if (!paymentModeExists) {
      throw new PaymentModeNotFoundException(categoryId);
    }
  }

  @Override
  public List<TransactionDto> getAllTransactions(String appUserId) {
    final var transactions = transactionRepo.findAllByAppUser(appUserId);
    return transactionMapper.transactionDtosToTransactionDtos(transactions);
  }

  @Override
  public TransactionDto updateTransaction(String appUserId, UpdateTransactionDto requestBody) {
    final var accountId = requestBody.accountId();
    final var categoryId = requestBody.categoryId();
    final var paymentModeId = requestBody.paymentModeId();
    final var toAccountId = requestBody.toAccountId();
    final var type = requestBody.type();

    final var accounts = getAccounts(accountId, toAccountId, type);
    validateAccountCategoryAndPaymentMode(appUserId, accounts, categoryId, paymentModeId);

    final var transaction = transactionRepo.findById(requestBody.transactionId())
        .orElseThrow(() -> new TransactionNotFoundException(requestBody.transactionId()));

    transactionMapper.updateTransactionFromDto(requestBody, transaction, appUserId);

    final var savedTransaction = transactionRepo.save(transaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  @Override
  public void deleteTransaction(String appUserId, Long transactionId) {
    transactionRepo.deleteByIdAndAppUserId(transactionId, appUserId);
  }
}
