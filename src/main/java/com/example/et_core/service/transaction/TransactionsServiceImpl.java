package com.example.et_core.service.transaction;

import com.example.et_core.dto.TransactionDto;
import com.example.et_core.dto.TransactionRequestDto;
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
  public TransactionDto saveTransaction(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException {

getAndValidateAccounts(requestBody, appUserId);

    if (TransactionType.valueOf(requestBody.type()) == TransactionType.TRANSFER) {
      return handleTransfer(appUserId, requestBody);
    }

    return handleExpenseOrIncome(appUserId, requestBody);
  }

  private void getAndValidateAccounts(TransactionRequestDto dto, String appUserId) {
    final var accountId = dto.accountId();
    final var categoryId = dto.categoryId();
    final var paymentModeId = dto.paymentModeId();
    final var toAccountId = dto.toAccountId();
    final var type = dto.type();

    final var accounts = getAccounts(accountId, toAccountId, type);

    validateAccountCategoryAndPaymentMode(appUserId, accounts, categoryId, paymentModeId);
  }

  private TransactionDto handleTransfer(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException {

    accountService.updateBalance(requestBody.accountId(), requestBody.amount(), requestBody.paymentModeId(), requestBody.type(), true);

    accountService.updateBalance(requestBody.toAccountId(), requestBody.amount(), requestBody.paymentModeId(), requestBody.type(), false);

    final var transferId = UUID.randomUUID().toString();

    final var debitTransaction = new Transaction();
    transactionMapper.transactionFromRequestDto(requestBody, debitTransaction, appUserId, transferId, true);

    final var creditTransaction = new Transaction();
    transactionMapper.transactionFromRequestDto(requestBody, creditTransaction, appUserId, transferId, false);

    transactionRepo.save(debitTransaction);
    final var savedTransaction = transactionRepo.save(creditTransaction);

    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  private TransactionDto handleExpenseOrIncome(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException {
    accountService.updateBalance(requestBody.toAccountId(), requestBody.amount(), requestBody.paymentModeId(), requestBody.type(), false);

    final var transaction = new Transaction();
    transactionMapper.transactionFromRequestDto(requestBody, transaction, appUserId, null, false);

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
  public TransactionDto updateTransaction(String appUserId, TransactionRequestDto requestBody) throws InsufficientAccountBalanceException {
    getAndValidateAccounts(requestBody, appUserId);

    final var transaction = transactionRepo.findById(requestBody.transactionId())
        .orElseThrow(() -> new TransactionNotFoundException(requestBody.transactionId()));

    if (TransactionType.valueOf(requestBody.type()) == TransactionType.TRANSFER) {
      return handleTransfer(appUserId, requestBody);
    }

    return handleExpenseOrIncome(appUserId, requestBody);

//    transactionMapper.transactionFromRequestDto(requestBody, transaction, appUserId, null, false);
//
//    final var savedTransaction = transactionRepo.save(transaction);
//
//    return transactionMapper.transactionDtoToTransactionDto(savedTransaction);
  }

  @Override
  public void deleteTransaction(String appUserId, Long transactionId) {
    transactionRepo.deleteByIdAndAppUserId(transactionId, appUserId);
  }
}
